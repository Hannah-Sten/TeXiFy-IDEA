package nl.hannahsten.texifyidea.run.latex.step

import com.intellij.execution.ExecutionException
import com.intellij.execution.process.ProcessHandler

internal interface LatexRunStep {

    val id: String

    @Throws(ExecutionException::class)
    fun createProcess(context: LatexRunStepContext): ProcessHandler
}
