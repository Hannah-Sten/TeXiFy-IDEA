package nl.hannahsten.texifyidea.run.latex.step

internal object LegacyExternalToolRunStepProvider : LatexRunStepProvider {

    override val type: String = "legacy-external-tool"

    override val aliases: Set<String> = setOf(
        type,
        "external-tool",
        "commandline",
    )

    override fun create(spec: LatexRunStepSpec): LatexRunStep = LegacyExternalToolRunStep()
}
