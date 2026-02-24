package nl.hannahsten.texifyidea.run.latex.step

import nl.hannahsten.texifyidea.run.latex.LatexCompileStepOptions
import nl.hannahsten.texifyidea.run.latex.LatexStepRunConfigurationOptions
import nl.hannahsten.texifyidea.run.latex.LatexStepType

internal object LatexCompileRunStepProvider : LatexRunStepProvider {

    override val type: String = LatexStepType.LATEX_COMPILE

    override val aliases: Set<String> = setOf(
        type,
        "compile-latex",
    )

    override fun create(stepConfig: LatexStepRunConfigurationOptions): LatexRunStep = LatexCompileRunStep(
        stepConfig as? LatexCompileStepOptions
            ?: error("Expected LatexCompileStepOptions for $type, but got ${stepConfig::class.simpleName}")
    )
}
