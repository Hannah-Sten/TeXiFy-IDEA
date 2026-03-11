package nl.hannahsten.texifyidea.run.latex.step

import com.intellij.execution.ExecutionException
import com.intellij.execution.process.ProcessHandler
import com.intellij.openapi.roots.ProjectRootManager
import com.intellij.util.execution.ParametersListUtil
import nl.hannahsten.texifyidea.run.common.createCompilationHandler
import nl.hannahsten.texifyidea.run.latex.BibtexStepOptions
import nl.hannahsten.texifyidea.run.latex.LatexRunConfiguration
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
        val command = mutableListOf(stepConfig.compilerPath ?: stepConfig.bibliographyCompiler.executableName)
        val session = context.session
        val distributionType = session.distributionType
        val mainFileDirectory = session.mainFile.parent.path
        if (stepConfig.bibliographyCompiler.name == "BIBER") {
            command += "--input-directory=$workingDirectory"
            command += "--output-directory=$workingDirectory"
        }
        else if (distributionType.isMiktex(session.project, session.mainFile)) {
            command += "-include-directory=$mainFileDirectory"
            command += ProjectRootManager.getInstance(session.project).contentSourceRoots
                .map { "-include-directory=${it.path}" }
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
        fun inferredWorkingDirectoryHint(runConfig: LatexRunConfiguration): Path? = CommandLineRunStep.inferredAuxiliaryWorkingDirectory(runConfig)
    }
}
