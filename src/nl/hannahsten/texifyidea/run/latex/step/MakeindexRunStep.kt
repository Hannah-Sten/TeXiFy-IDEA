package nl.hannahsten.texifyidea.run.latex.step

import com.intellij.execution.ExecutionException
import com.intellij.execution.process.ProcessHandler
import com.intellij.execution.util.ProgramParametersConfigurator
import com.intellij.util.execution.ParametersListUtil
import nl.hannahsten.texifyidea.run.common.createCompilationHandler
import nl.hannahsten.texifyidea.run.compiler.MakeindexProgram
import nl.hannahsten.texifyidea.run.latex.flow.BaseLatexStepExecution
import nl.hannahsten.texifyidea.run.latex.flow.ProcessLatexStepExecution
import nl.hannahsten.texifyidea.run.latex.MakeindexStepOptions
import nl.hannahsten.texifyidea.run.latex.getMakeindexOptions
import nl.hannahsten.texifyidea.util.appendExtension

internal class MakeindexRunStep(
    private val stepConfig: MakeindexStepOptions,
) : LatexRunStep {

    override val configId: String = stepConfig.id
    override val id: String = stepConfig.type

    @Throws(ExecutionException::class)
    override fun createStepExecution(index: Int, context: LatexRunStepContext): BaseLatexStepExecution {
        val artifactSync = StepArtifactSync(context, stepConfig)
        return ProcessLatexStepExecution(
            index = index,
            type = id,
            displayName = LatexStepPresentation.displayName(id),
            configId = configId,
            processHandler = createProcess(context),
            beforeStart = { artifactSync.beforeStep() },
            afterFinish = { exitCode -> artifactSync.afterStep(exitCode) },
        )
    }

    @Throws(ExecutionException::class)
    private fun createProcess(context: LatexRunStepContext): ProcessHandler {
        val command = buildCommand(context)
        val workingDirectory = CommandLineRunStep.resolveWorkingDirectory(context, stepConfig.workingDirectoryPath)
        val configurator = ProgramParametersConfigurator()
        return createCompilationHandler(
            environment = context.environment,
            mainFile = context.mainFile,
            command = command,
            workingDirectory = workingDirectory,
            expandMacrosEnvVariables = context.runConfig.expandMacrosEnvVariables,
            envs = context.runConfig.environmentVariables.envs,
            expandEnvValue = { value -> configurator.expandPathAndMacros(value, null, context.runConfig.project) ?: value },
        )
    }

    internal fun buildCommand(context: LatexRunStepContext): List<String> {
        val makeindexOptions = getMakeindexOptions(context.mainFile, context.runConfig.project)
        val baseFileName = stepConfig.targetBaseNameOverride
            ?.trim()
            ?.takeIf(String::isNotBlank)
            ?: makeindexOptions["name"]
            ?: context.mainFile.nameWithoutExtension

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
