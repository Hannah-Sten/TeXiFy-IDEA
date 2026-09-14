package nl.hannahsten.texifyidea.run.latex.step

import com.intellij.execution.ExecutionException
import com.intellij.execution.configurations.GeneralCommandLine
import com.intellij.execution.process.ProcessHandler
import com.intellij.openapi.roots.ProjectRootManager
import com.intellij.util.execution.ParametersListUtil
import nl.hannahsten.texifyidea.run.common.createCompilationHandler
import nl.hannahsten.texifyidea.run.compiler.LatexCompiler.Companion.toWslPathIfNeeded
import nl.hannahsten.texifyidea.run.latex.BibtexStepOptions
import nl.hannahsten.texifyidea.run.latex.LatexDistributionType
import nl.hannahsten.texifyidea.run.latex.LatexRunConfiguration
import nl.hannahsten.texifyidea.util.SystemEnvironment
import java.io.File
import java.nio.file.Path

internal class BibtexRunStep(
    private val stepConfig: BibtexStepOptions,
) : ProcessLatexRunStep {

    override val configId: String = stepConfig.id
    override val id: String = stepConfig.type

    @Throws(ExecutionException::class)
    override fun createProcess(context: LatexRunStepContext): ProcessHandler {
        val workingDirectory = CommandLineRunStep.resolveAuxiliaryWorkingDirectory(context, stepConfig.workingDirectoryPath)
        val command = buildCommand(context, workingDirectory)
        val extraEnvironment = buildExtraEnvironment(context, workingDirectory)

        return createCompilationHandler(
            context = context,
            command = command,
            workingDirectory = workingDirectory,
            extraEnvironment = extraEnvironment,
        )
    }

    internal fun buildCommand(context: LatexRunStepContext, workingDirectory: Path): List<String> {
        val session = context.session
        val distributionType = session.distributionType
        val mainFileDirectory = session.mainFile.parent.path.toWslPathIfNeeded(distributionType)
        val workingDirectoryPath = workingDirectory.toString().toWslPathIfNeeded(distributionType)
        val command = mutableListOf(stepConfig.compilerPath ?: stepConfig.bibliographyCompiler.executableName)
        if (stepConfig.bibliographyCompiler.name == "BIBER") {
            command += "--input-directory=$mainFileDirectory"
            command += "--output-directory=$workingDirectoryPath"
        }
        else if (distributionType.isMiktex(session.project, session.mainFile)) {
            command += "-include-directory=$mainFileDirectory"
            command += ProjectRootManager.getInstance(session.project).contentSourceRoots
                .map { "-include-directory=${it.path.toWslPathIfNeeded(distributionType)}" }
        }

        if (distributionType == LatexDistributionType.WSL_TEXLIVE) {
            var wslCommand = GeneralCommandLine(command).commandLineString
            stepConfig.compilerArguments
                ?.takeIf(String::isNotBlank)
                ?.let { arguments ->
                    ParametersListUtil.parse(arguments).forEach { wslCommand += " $it" }
                }
            wslCommand += " ${session.mainFile.nameWithoutExtension}"
            return mutableListOf(*SystemEnvironment.wslCommand, wslCommand)
        }

        stepConfig.compilerArguments
            ?.takeIf(String::isNotBlank)
            ?.let { command += ParametersListUtil.parse(it) }
        command += context.session.mainFile.nameWithoutExtension
        return command
    }

    internal fun buildExtraEnvironment(context: LatexRunStepContext, workingDirectory: Path): Map<String, String> {
        val session = context.session
        if (session.distributionType.isMiktex(session.project, session.mainFile)) {
            return emptyMap()
        }

        val mainFileDirectory = session.mainFile.parent.path
        if (workingDirectory == Path.of(mainFileDirectory)) {
            return emptyMap()
        }

        val currentEnv = context.runConfig.environmentVariables.envs
        return mapOf(
            "BIBINPUTS" to prependDirectory(mainFileDirectory, currentEnv["BIBINPUTS"]),
            "BSTINPUTS" to prependRaw("$mainFileDirectory${File.pathSeparator}", currentEnv["BSTINPUTS"]),
        )
    }

    private fun prependDirectory(directory: String, existing: String?): String {
        val trimmedExisting = existing?.trim()?.takeIf(String::isNotBlank)
        return if (trimmedExisting == null) directory else directory + File.pathSeparator + trimmedExisting
    }

    private fun prependRaw(prefix: String, existing: String?): String {
        val trimmedExisting = existing?.trim()?.takeIf(String::isNotBlank)
        return if (trimmedExisting == null) prefix else prefix + trimmedExisting
    }

    companion object {
        fun inferredWorkingDirectoryHint(runConfig: LatexRunConfiguration): String = runConfig.rawAuxPathOrOutputPathForUiHint()
    }
}
