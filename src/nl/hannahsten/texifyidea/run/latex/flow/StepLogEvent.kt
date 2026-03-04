package nl.hannahsten.texifyidea.run.latex.flow

import com.intellij.openapi.util.Key
import nl.hannahsten.texifyidea.run.latex.step.LatexRunStep

internal sealed class StepLogEvent {

    data class StepStarted(
        val index: Int,
        val step: LatexRunStep,
    ) : StepLogEvent()

    data class StepOutput(
        val index: Int,
        val step: LatexRunStep,
        val text: String,
        val outputType: Key<*>,
    ) : StepLogEvent()

    data class StepFinished(
        val index: Int,
        val step: LatexRunStep,
        val exitCode: Int,
    ) : StepLogEvent()

    data class RunFinished(
        val exitCode: Int,
    ) : StepLogEvent()
}
