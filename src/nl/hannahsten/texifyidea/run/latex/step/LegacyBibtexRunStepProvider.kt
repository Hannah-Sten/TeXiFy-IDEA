package nl.hannahsten.texifyidea.run.latex.step

import nl.hannahsten.texifyidea.run.latex.BibtexStepConfig
import nl.hannahsten.texifyidea.run.latex.LatexStepConfig
import nl.hannahsten.texifyidea.run.latex.LatexStepType

internal object LegacyBibtexRunStepProvider : LatexRunStepProvider {

    override val type: String = LatexStepType.BIBTEX

    override val aliases: Set<String> = setOf(
        type,
        "legacy-bibtex",
        "bibliography",
    )

    override fun create(stepConfig: LatexStepConfig): LatexRunStep = BibtexRunStep(
        stepConfig as? BibtexStepConfig
            ?: error("Expected BibtexStepConfig for $type, but got ${stepConfig::class.simpleName}")
    )
}
