package nl.hannahsten.texifyidea.run.latex.step

import nl.hannahsten.texifyidea.run.latex.BibtexStepOptions
import nl.hannahsten.texifyidea.run.latex.LatexStepRunConfigurationOptions
import nl.hannahsten.texifyidea.run.latex.LatexStepType

internal object LegacyBibtexRunStepProvider : LatexRunStepProvider {

    override val type: String = LatexStepType.BIBTEX

    override val aliases: Set<String> = setOf(
        type,
        "legacy-bibtex",
        "bibliography",
    )

    override fun create(stepConfig: LatexStepRunConfigurationOptions): LatexRunStep = BibtexRunStep(
        stepConfig as? BibtexStepOptions
            ?: error("Expected BibtexStepOptions for $type, but got ${stepConfig::class.simpleName}")
    )
}
