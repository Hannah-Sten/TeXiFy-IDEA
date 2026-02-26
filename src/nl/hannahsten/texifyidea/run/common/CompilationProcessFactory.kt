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
import com.intellij.openapi.actionSystem.impl.SimpleDataContext
import com.intellij.openapi.util.SystemInfo
import nl.hannahsten.texifyidea.run.latex.step.LatexRunStepContext
import nl.hannahsten.texifyidea.util.Log
import java.nio.file.Path
import kotlin.io.path.exists

@Throws(ExecutionException::class)
internal fun createCompilationHandler(
    context: LatexRunStepContext,
    command: List<String>,
    workingDirectory: Path? = context.session.workingDirectory,
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

    val envVariables = if (!runConfig.expandMacrosEnvVariables) {
        runConfig.environmentVariables.envs
    }
    else {
        val programParamsConfigurator = ProgramParametersConfigurator()
        ExecutionManagerImpl.withEnvironmentDataContext(
            SimpleDataContext.getSimpleContext(CommonDataKeys.VIRTUAL_FILE, mainFile, environment.dataContext),
        ).use {
            runConfig.environmentVariables.envs.mapValues { (_, value) ->
                programParamsConfigurator.expandPathAndMacros(value, null, runConfig.project) ?: value
            }
        }
    }

    if (SystemInfo.isWindows && command.sumOf { it.length } > 10_000) {
        throw ExecutionException("The following command was too long to run: ${command.joinToString(" ")}")
    }

    val commandLine = GeneralCommandLine(command)
        .withWorkingDirectory(resolvedWorkingDirectory)
        .withParentEnvironmentType(GeneralCommandLine.ParentEnvironmentType.CONSOLE)
        .withEnvironment(envVariables)

    Log.debug("Executing ${commandLine.commandLineString} in $resolvedWorkingDirectory")

    val handler = KillableProcessHandler(commandLine)
    ProcessTerminatedListener.attach(handler, environment.project)

    return handler
}
