package nl.hannahsten.texifyidea.run.latex.flow

import com.intellij.execution.process.ProcessHandler

internal sealed interface LatexStepExecution {

    val index: Int
    val type: String
    val displayName: String
    val configId: String
    val beforeStart: () -> Unit
    val afterFinish: (exitCode: Int) -> Unit
}

internal abstract class BaseLatexStepExecution(
    override val index: Int,
    override val type: String,
    override val displayName: String,
    override val configId: String,
    override val beforeStart: () -> Unit = {},
    override val afterFinish: (exitCode: Int) -> Unit = {},
) : LatexStepExecution

internal class ProcessLatexStepExecution(
    index: Int,
    type: String,
    displayName: String,
    configId: String,
    val processHandler: ProcessHandler,
    beforeStart: () -> Unit = {},
    afterFinish: (exitCode: Int) -> Unit = {},
) : BaseLatexStepExecution(
    index = index,
    type = type,
    displayName = displayName,
    configId = configId,
    beforeStart = beforeStart,
    afterFinish = afterFinish,
)

internal class InlineLatexStepExecution(
    index: Int,
    type: String,
    displayName: String,
    configId: String,
    val action: () -> Int = { 0 },
    beforeStart: () -> Unit = {},
    afterFinish: (exitCode: Int) -> Unit = {},
) : BaseLatexStepExecution(
    index = index,
    type = type,
    displayName = displayName,
    configId = configId,
    beforeStart = beforeStart,
    afterFinish = afterFinish,
)
