package nl.hannahsten.texifyidea.run.latex.step

import nl.hannahsten.texifyidea.util.appendExtension
import nl.hannahsten.texifyidea.run.latex.LatexStepConfig
import nl.hannahsten.texifyidea.run.latex.LatexStepType
import nl.hannahsten.texifyidea.run.latex.XindyStepConfig

internal object XindyCommandRunStepProvider : LatexRunStepProvider {

    override val type: String = LatexStepType.XINDY

    override val aliases: Set<String> = setOf(
        type,
        "xindy-command",
        "xindy",
        "texindy",
    )

    override fun create(stepConfig: LatexStepConfig): LatexRunStep = CommandLineRunStep(
        configId = stepConfig.id,
        id = type,
        commandLineSupplier = { context ->
            (stepConfig as? XindyStepConfig)?.commandLine
                ?.takeIf(String::isNotBlank)
                ?: "xindy ${context.mainFile.nameWithoutExtension.appendExtension("idx")}"
        },
    )
}
