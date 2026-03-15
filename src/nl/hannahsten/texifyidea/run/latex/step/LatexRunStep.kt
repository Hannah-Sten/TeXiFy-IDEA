package nl.hannahsten.texifyidea.run.latex.step

import com.intellij.execution.ExecutionException
import com.intellij.execution.process.ProcessHandler

/**
 * Common contract for executable units in the LaTeX run pipeline.
 * Implementations are created from persisted step options and executed in sequence.
 */
internal interface LatexRunStep {

    val configId: String
    val id: String
    val displayName: String
        get() = LatexStepPresentation.displayName(id)

    @Throws(ExecutionException::class)
    fun createProcess(context: LatexRunStepContext): ProcessHandler? = null

    fun beforeStart(context: LatexRunStepContext) {}

    fun afterFinish(context: LatexRunStepContext, exitCode: Int) {}
}

/**
 * Step contract for commands backed by an IntelliJ [ProcessHandler].
 * The sequential handler starts and monitors these steps as external processes.
 */
internal interface ProcessLatexRunStep : LatexRunStep {

    @Throws(ExecutionException::class)
    override fun createProcess(context: LatexRunStepContext): ProcessHandler
}
