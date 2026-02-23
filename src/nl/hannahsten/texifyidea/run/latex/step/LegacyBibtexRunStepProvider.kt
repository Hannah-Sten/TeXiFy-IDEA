package nl.hannahsten.texifyidea.run.latex.step

internal object LegacyBibtexRunStepProvider : LatexRunStepProvider {

    override val type: String = "legacy-bibtex"

    override val aliases: Set<String> = setOf(
        type,
        "bibliography",
    )

    override fun create(spec: LatexRunStepSpec): LatexRunStep = LegacyBibtexRunStep()
}
