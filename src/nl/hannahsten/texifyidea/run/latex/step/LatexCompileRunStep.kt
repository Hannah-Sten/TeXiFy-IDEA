package nl.hannahsten.texifyidea.run.latex.step

import com.intellij.execution.ExecutionException
import com.intellij.execution.process.ProcessHandler
import nl.hannahsten.texifyidea.run.common.createCompilationHandler
import nl.hannahsten.texifyidea.run.latex.LatexCompileStepOptions
import nl.hannahsten.texifyidea.run.latex.LatexSessionInitializer

internal class LatexCompileRunStep(
    private val stepConfig: LatexCompileStepOptions,
) : ProcessLatexRunStep {

    override val configId: String = stepConfig.id
    override val id: String = stepConfig.type

    @Throws(ExecutionException::class)
    override fun createProcess(context: LatexRunStepContext): ProcessHandler {
        val session = context.session
        LatexSessionInitializer.updateOutputFilePath(session, stepConfig)

        val command = stepConfig.compiler.buildCommand(session, stepConfig)

        return createCompilationHandler(
            context = context,
            command = command,
            workingDirectory = session.workingDirectory,
        )
    }
}
