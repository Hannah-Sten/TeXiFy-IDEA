package nl.hannahsten.texifyidea.run.latex.step

import nl.hannahsten.texifyidea.run.latex.LatexCompileStepConfig
import nl.hannahsten.texifyidea.run.latex.LatexStepConfig
import nl.hannahsten.texifyidea.run.latex.LatexStepType

internal object LatexCompileRunStepProvider : LatexRunStepProvider {

    override val type: String = LatexStepType.LATEX_COMPILE

    override val aliases: Set<String> = setOf(
        type,
        "compile-latex",
    )

    override fun create(stepConfig: LatexStepConfig): LatexRunStep = LatexCompileRunStep(
        stepConfig as? LatexCompileStepConfig
            ?: error("Expected LatexCompileStepConfig for $type, but got ${stepConfig::class.simpleName}")
    )
}
