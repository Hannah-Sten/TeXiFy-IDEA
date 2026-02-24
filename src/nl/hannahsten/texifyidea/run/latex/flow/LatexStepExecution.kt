package nl.hannahsten.texifyidea.run.latex.flow

import com.intellij.execution.process.ProcessHandler

internal data class LatexStepExecution(
    val index: Int,
    val type: String,
    val displayName: String,
    val configId: String,
    val processHandler: ProcessHandler,
    val beforeStart: () -> Unit = {},
    val afterFinish: (exitCode: Int) -> Unit = {},
) {

    constructor(
        index: Int,
        type: String,
        displayName: String,
        configId: String,
        processHandler: ProcessHandler,
    ) : this(
        index = index,
        type = type,
        displayName = displayName,
        configId = configId,
        processHandler = processHandler,
        beforeStart = {},
        afterFinish = {},
    )
}
