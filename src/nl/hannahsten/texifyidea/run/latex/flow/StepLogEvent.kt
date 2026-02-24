package nl.hannahsten.texifyidea.run.latex.flow

import com.intellij.openapi.util.Key

internal sealed class StepLogEvent {

    data class StepStarted(
        val execution: LatexStepExecution,
    ) : StepLogEvent()

    data class StepOutput(
        val execution: LatexStepExecution,
        val text: String,
        val outputType: Key<*>,
    ) : StepLogEvent()

    data class StepFinished(
        val execution: LatexStepExecution,
        val exitCode: Int,
    ) : StepLogEvent()

    data class RunFinished(
        val exitCode: Int,
    ) : StepLogEvent()
}
