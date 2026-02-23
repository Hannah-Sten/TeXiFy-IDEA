package nl.hannahsten.texifyidea.run.latex.step

internal object PythontexCommandRunStepProvider : LatexRunStepProvider {

    override val type: String = "pythontex-command"

    override val aliases: Set<String> = setOf(
        type,
        "pythontex",
    )

    override fun create(spec: LatexRunStepSpec): LatexRunStep = CommandLineRunStep(
        id = type,
        commandLineSupplier = { context -> "pythontex ${context.mainFile.nameWithoutExtension}" },
    )
}
