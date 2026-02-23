package nl.hannahsten.texifyidea.run.latex.step

import nl.hannahsten.texifyidea.util.appendExtension

internal object XindyCommandRunStepProvider : LatexRunStepProvider {

    override val type: String = "xindy-command"

    override val aliases: Set<String> = setOf(
        type,
        "xindy",
        "texindy",
    )

    override fun create(spec: LatexRunStepSpec): LatexRunStep = CommandLineRunStep(
        id = type,
        commandLineSupplier = { context -> "xindy ${context.mainFile.nameWithoutExtension.appendExtension("idx")}" },
    )
}
