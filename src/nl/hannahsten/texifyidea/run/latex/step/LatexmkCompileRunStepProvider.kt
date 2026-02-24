package nl.hannahsten.texifyidea.run.latex.step

import nl.hannahsten.texifyidea.run.latex.LatexStepConfig
import nl.hannahsten.texifyidea.run.latex.LatexStepType
import nl.hannahsten.texifyidea.run.latex.LatexmkCompileStepConfig

internal object LatexmkCompileRunStepProvider : LatexRunStepProvider {

    override val type: String = LatexStepType.LATEXMK_COMPILE

    override val aliases: Set<String> = setOf(
        type,
        "compile-latexmk",
        "latexmk",
    )

    override fun create(stepConfig: LatexStepConfig): LatexRunStep = LatexCompileRunStep(
        stepConfig as? LatexmkCompileStepConfig
            ?: error("Expected LatexmkCompileStepConfig for $type, but got ${stepConfig::class.simpleName}")
    )
}
