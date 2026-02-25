package nl.hannahsten.texifyidea.run.latex.step

import com.intellij.execution.ExecutionException
import nl.hannahsten.texifyidea.run.latex.flow.BaseLatexStepExecution

internal interface LatexRunStep {

    val configId: String
    val id: String

    @Throws(ExecutionException::class)
    fun createStepExecution(index: Int, context: LatexRunStepContext): BaseLatexStepExecution
}
