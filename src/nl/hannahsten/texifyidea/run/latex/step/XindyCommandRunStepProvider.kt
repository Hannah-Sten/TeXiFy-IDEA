package nl.hannahsten.texifyidea.run.latex.step

import nl.hannahsten.texifyidea.util.appendExtension
import nl.hannahsten.texifyidea.run.latex.LatexStepRunConfigurationOptions
import nl.hannahsten.texifyidea.run.latex.LatexStepType
import nl.hannahsten.texifyidea.run.latex.XindyStepOptions

internal object XindyCommandRunStepProvider : LatexRunStepProvider {

    override val type: String = LatexStepType.XINDY

    override val aliases: Set<String> = setOf(
        type,
        "xindy-command",
        "xindy",
        "texindy",
    )

    override fun create(stepConfig: LatexStepRunConfigurationOptions): LatexRunStep = CommandLineRunStep(
        configId = stepConfig.id,
        id = type,
        commandLineSupplier = { context ->
            (stepConfig as? XindyStepOptions)?.commandLine
                ?.takeIf(String::isNotBlank)
                ?: "xindy ${context.mainFile.nameWithoutExtension.appendExtension("idx")}"
        },
    )
}
