package nl.hannahsten.texifyidea.run.latex.step

import com.intellij.execution.ExecutionException
import com.intellij.execution.process.ProcessHandler

internal interface LatexRunStep {

    val configId: String
    val id: String
    val displayName: String
        get() = LatexStepPresentation.displayName(id)

    fun beforeStart(context: LatexRunStepContext) {}

    fun afterFinish(context: LatexRunStepContext, exitCode: Int) {}
}

internal interface ProcessLatexRunStep : LatexRunStep {

    @Throws(ExecutionException::class)
    fun createProcess(context: LatexRunStepContext): ProcessHandler
}

internal interface InlineLatexRunStep : LatexRunStep {

    @Throws(ExecutionException::class)
    fun runInline(context: LatexRunStepContext): Int
}
