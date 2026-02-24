package nl.hannahsten.texifyidea.run.latex.step

import nl.hannahsten.texifyidea.run.latex.LatexStepRunConfigurationOptions
import nl.hannahsten.texifyidea.run.latex.LatexStepType
import nl.hannahsten.texifyidea.run.latex.LatexmkCompileStepOptions

internal object LatexmkCompileRunStepProvider : LatexRunStepProvider {

    override val type: String = LatexStepType.LATEXMK_COMPILE

    override val aliases: Set<String> = setOf(
        type,
        "compile-latexmk",
        "latexmk",
    )

    override fun create(stepConfig: LatexStepRunConfigurationOptions): LatexRunStep = LatexCompileRunStep(
        stepConfig as? LatexmkCompileStepOptions
            ?: error("Expected LatexmkCompileStepOptions for $type, but got ${stepConfig::class.simpleName}")
    )
}
