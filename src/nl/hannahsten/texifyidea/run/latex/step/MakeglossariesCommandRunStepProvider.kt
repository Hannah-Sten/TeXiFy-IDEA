package nl.hannahsten.texifyidea.run.latex.step

internal object MakeglossariesCommandRunStepProvider : LatexRunStepProvider {

    override val type: String = "makeglossaries-command"

    override val aliases: Set<String> = setOf(
        type,
        "makeglossaries",
    )

    override fun create(spec: LatexRunStepSpec): LatexRunStep = CommandLineRunStep(
        id = type,
        commandLineSupplier = { context -> "makeglossaries ${context.mainFile.nameWithoutExtension}" },
    )
}
