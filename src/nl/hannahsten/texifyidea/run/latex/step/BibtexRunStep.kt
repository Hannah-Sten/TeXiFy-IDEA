package nl.hannahsten.texifyidea.run.latex.step

import com.intellij.execution.ExecutionException
import com.intellij.execution.process.ProcessHandler
import com.intellij.util.execution.ParametersListUtil
import nl.hannahsten.texifyidea.run.common.createCompilationHandler
import nl.hannahsten.texifyidea.run.latex.BibtexStepOptions
import nl.hannahsten.texifyidea.run.latex.LatexRunConfiguration
import java.nio.file.Path

internal class BibtexRunStep(
    private val stepConfig: BibtexStepOptions,
) : ProcessLatexRunStep {

    override val configId: String = stepConfig.id
    override val id: String = stepConfig.type

    @Throws(ExecutionException::class)
    override fun createProcess(context: LatexRunStepContext): ProcessHandler {
        val workingDirectory = CommandLineRunStep.resolveAuxiliaryWorkingDirectory(context, stepConfig.workingDirectoryPath)
        val command = mutableListOf(stepConfig.compilerPath ?: stepConfig.bibliographyCompiler.executableName)
        if (stepConfig.bibliographyCompiler.name == "BIBER") {
            val pathText = context.session.mainFile.parent.path
            command += "--input-directory=$pathText"
        }
        stepConfig.compilerArguments
            ?.takeIf(String::isNotBlank)
            ?.let { command += ParametersListUtil.parse(it) }
        command += context.session.mainFile.nameWithoutExtension

        return createCompilationHandler(
            context = context,
            command = command,
            workingDirectory = workingDirectory,
        )
    }

    companion object {
        fun inferredWorkingDirectoryHint(runConfig: LatexRunConfiguration): Path? = CommandLineRunStep.inferredAuxiliaryWorkingDirectory(runConfig)
    }
}
