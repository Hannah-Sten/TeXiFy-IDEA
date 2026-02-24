package nl.hannahsten.texifyidea.run.latex.flow

import com.intellij.execution.process.ProcessHandler

internal data class LatexStepExecution(
    val index: Int,
    val type: String,
    val displayName: String,
    val processHandler: ProcessHandler,
)
