package nl.hannahsten.texifyidea.run.latex.step

internal object LegacyMakeindexRunStepProvider : LatexRunStepProvider {

    override val type: String = "legacy-makeindex"

    override val aliases: Set<String> = setOf(
        type,
        "index",
        "makeindex",
    )

    override fun create(spec: LatexRunStepSpec): LatexRunStep = LegacyMakeindexRunStep()
}
