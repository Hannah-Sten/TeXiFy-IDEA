package nl.hannahsten.texifyidea.run.common

import com.intellij.execution.ExecutionException
import com.intellij.execution.configurations.GeneralCommandLine
import com.intellij.execution.impl.ExecutionManagerImpl
import com.intellij.execution.process.KillableProcessHandler
import com.intellij.execution.process.ProcessTerminatedListener
import com.intellij.execution.util.ProgramParametersConfigurator
import com.intellij.notification.Notification
import com.intellij.notification.NotificationType
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.actionSystem.DataContext
import com.intellij.openapi.actionSystem.impl.SimpleDataContext
import com.intellij.openapi.module.ModuleUtil
import com.intellij.openapi.project.rootManager
import com.intellij.openapi.util.SystemInfo
import com.intellij.openapi.vfs.VirtualFile
import nl.hannahsten.texifyidea.run.compiler.LatexCompiler.Companion.toWslPathIfNeeded
import nl.hannahsten.texifyidea.run.latex.LatexDistributionType
import nl.hannahsten.texifyidea.run.latex.LatexRunConfiguration
import nl.hannahsten.texifyidea.run.latex.step.LatexRunStepContext
import nl.hannahsten.texifyidea.settings.sdk.WslPathUtil.windowsPathToWsl
import nl.hannahsten.texifyidea.util.Log
import nl.hannahsten.texifyidea.util.containsAny
import nl.hannahsten.texifyidea.util.inWsl
import java.io.File
import java.nio.file.Path
import kotlin.io.path.exists

@Throws(ExecutionException::class)
internal fun createCompilationHandler(
    context: LatexRunStepContext,
    command: List<String>,
    workingDirectory: Path? = context.session.workingDirectory,
    extraEnvironment: Map<String, String> = emptyMap(),
): KillableProcessHandler {
    val runConfig = context.runConfig
    val mainFile = context.session.mainFile
    val environment = context.environment
    val resolvedWorkingDirectory = workingDirectory ?: Path.of(mainFile.parent.path)

    if (resolvedWorkingDirectory.exists().not()) {
        Notification(
            "LaTeX",
            "Could not find working directory",
            "The directory containing the main file could not be found: $resolvedWorkingDirectory",
            NotificationType.ERROR,
        ).notify(environment.project)
        throw ExecutionException("Could not find working directory $resolvedWorkingDirectory for file $mainFile")
    }

    val envVariables = expandEnvironmentVariables(runConfig, mainFile, environment.dataContext)

    if (SystemInfo.isWindows && command.sumOf { it.length } > 10_000) {
        throw ExecutionException("The following command was too long to run: ${command.joinToString(" ")}")
    }

    val commandList = command.toMutableList()

    val distributionType = runConfig.getLatexDistributionType()
    // The GeneralCommandLine requires a Windows working directory and cannot handle //wsl paths, so we need to set the working directory within WSL
    // This holds for any command run through WSL, hence we apply the fix here
    val compilerWorkingDirectory = if (distributionType == LatexDistributionType.WSL_TEXLIVE && resolvedWorkingDirectory.toString().inWsl() && commandList.first() == "wsl" && commandList.getOrNull(1) == "bash") {
        commandList.removeLast()
        commandList.add("cd " + windowsPathToWsl(resolvedWorkingDirectory.toString()) + " && " + command.last())
        Path.of(System.getProperty("user.home"))
    }
    else {
        resolvedWorkingDirectory
    }

    val commandLine = GeneralCommandLine(commandList)
        .withWorkingDirectory(compilerWorkingDirectory)
        .withParentEnvironmentType(GeneralCommandLine.ParentEnvironmentType.CONSOLE)
        .withEnvironment(envVariables + extraEnvironment)

    Log.debug("Executing ${commandLine.commandLineString} in $resolvedWorkingDirectory")

    val handler = KillableProcessHandler(commandLine)
    ProcessTerminatedListener.attach(handler, environment.project)

    return handler
}

fun expandEnvironmentVariables(
    runConfig: LatexRunConfiguration,
    mainFile: VirtualFile?,
    context: DataContext?
): Map<String?, String?> {
    val envVariables = if (!runConfig.expandMacrosEnvVariables) {
        runConfig.environmentVariables.envs
    }
    else {
        val programParamsConfigurator = ProgramParametersConfigurator()
        val expandMacros = {
            runConfig.environmentVariables.envs.mapValues { (_, value) ->
                programParamsConfigurator.expandPathAndMacros(value, null, runConfig.project) ?: value
            }
        }

        if (mainFile != null) {
            ExecutionManagerImpl.withEnvironmentDataContext(
                SimpleDataContext.getSimpleContext(CommonDataKeys.VIRTUAL_FILE, mainFile, context),
            ).use {
                expandMacros()
            }
        }
        else {
            expandMacros()
        }
    }
    val result = envVariables.toMutableMap()

    // Add source roots to TEXINPUTS.
    val distributionType = runConfig.getLatexDistributionType()
    if (mainFile != null && !distributionType.isDocker()) {
        val allRoots = ModuleUtil.findModuleForFile(mainFile, runConfig.project)?.rootManager?.sourceRoots ?: emptyArray()
        if (allRoots.isNotEmpty()) {
            val texinputs = result["TEXINPUTS"] ?: ""

            // Limit the number of roots, in case the user has hundreds of roots it can exceed the maximum length on Windows.
            // We use 10,000 as a safe limit, similar to the command line length limit.
            var totalLength = texinputs.length
            val rootsToAdd = mutableListOf<String>()
            for (root in allRoots) {
                val path = root.path.toWslPathIfNeeded(distributionType)
                totalLength += path.length + 1
                if (SystemInfo.isWindows && totalLength > 10_000) {
                    // Including a random subset would be a confusing user experience, so we just try to guess something that maybe works
                    Log.debug("Too many source roots to add to TEXINPUTS: found ${allRoots.size} roots.")
                    rootsToAdd.clear()
                    rootsToAdd.addAll(allRoots.filter { it.name.lowercase().containsAny(setOf("tex", "latex", "bib", "bibtex")) }.map { it.path.toWslPathIfNeeded(distributionType) })
                    break
                }
                rootsToAdd.add(path)
            }

            if (rootsToAdd.isNotEmpty()) {
                val separator = if (distributionType == LatexDistributionType.WSL_TEXLIVE) ":" else File.pathSeparator
                val joinedRoots = rootsToAdd.joinToString(separator)
                // Append the source roots to TEXINPUTS.
                // We also add a trailing separator to make sure the original TEXINPUTS (or default) is still included.
                result["TEXINPUTS"] = if (texinputs.isBlank()) "$joinedRoots$separator" else "$texinputs$separator$joinedRoots$separator"
            }
        }
    }

    return result
}
