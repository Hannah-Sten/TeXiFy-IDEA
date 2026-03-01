package nl.hannahsten.texifyidea.run.latex.step

import com.intellij.execution.ExecutionException
import com.intellij.execution.process.ProcessHandler
import com.intellij.util.execution.ParametersListUtil
import nl.hannahsten.texifyidea.run.common.createCompilationHandler
import nl.hannahsten.texifyidea.run.compiler.MakeindexProgram
import nl.hannahsten.texifyidea.run.latex.MakeindexStepOptions
import nl.hannahsten.texifyidea.run.latex.getMakeindexOptions
import nl.hannahsten.texifyidea.util.appendExtension
import java.nio.file.Path

internal class MakeindexRunStep(
    private val stepConfig: MakeindexStepOptions,
) : ProcessLatexRunStep {

    override val configId: String = stepConfig.id
    override val id: String = stepConfig.type

    override fun beforeStart(context: LatexRunStepContext) {
        StepArtifactSync(context, stepConfig).beforeStep()
    }

    override fun afterFinish(context: LatexRunStepContext, exitCode: Int) {
        StepArtifactSync(context, stepConfig).afterStep(exitCode)
    }

    @Throws(ExecutionException::class)
    override fun createProcess(context: LatexRunStepContext): ProcessHandler {
        val command = buildCommand(context)
        val workingDirectory = resolveWorkingDirectory(context)
        return createCompilationHandler(
            context = context,
            command = command,
            workingDirectory = workingDirectory,
        )
    }

    internal fun resolveWorkingDirectory(context: LatexRunStepContext): Path {
        val configuredPath = stepConfig.workingDirectoryPath
        if (stepConfig.program == MakeindexProgram.BIB2GLS && configuredPath.isNullOrBlank()) {
            return Path.of(context.session.mainFile.parent.path)
        }
        return CommandLineRunStep.resolveWorkingDirectory(context, configuredPath)
    }

    internal fun buildCommand(context: LatexRunStepContext): List<String> {
        val makeindexOptions = getMakeindexOptions(context.session.mainFile, context.runConfig.project)
        val baseFileName = stepConfig.targetBaseNameOverride
            ?.trim()
            ?.takeIf(String::isNotBlank)
            ?: makeindexOptions["name"]
            ?: context.session.mainFile.nameWithoutExtension

        val command = mutableListOf(stepConfig.program.executableName)
        val parsedArguments = stepConfig.commandLineArguments
            ?.takeIf(String::isNotBlank)
            ?.let(ParametersListUtil::parse)
            .orEmpty()
        command += parsedArguments
        val overridesOutputFile = parsedArguments.any { it == "-o" || it.startsWith("-o") }
        if (!overridesOutputFile) {
            command += when (stepConfig.program) {
                MakeindexProgram.XINDY -> baseFileName.appendExtension("idx")
                else -> baseFileName
            }
        }
        return command
    }
}
