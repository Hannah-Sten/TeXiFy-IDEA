package nl.hannahsten.texifyidea.run.latex.step

import nl.hannahsten.texifyidea.run.latex.LatexStepConfig
import nl.hannahsten.texifyidea.run.latex.LatexStepType
import nl.hannahsten.texifyidea.run.latex.MakeglossariesStepConfig

internal object MakeglossariesCommandRunStepProvider : LatexRunStepProvider {

    override val type: String = LatexStepType.MAKEGLOSSARIES

    override val aliases: Set<String> = setOf(
        type,
        "makeglossaries-command",
        "makeglossaries",
    )

    override fun create(stepConfig: LatexStepConfig): LatexRunStep = CommandLineRunStep(
        configId = stepConfig.id,
        id = type,
        commandLineSupplier = { context ->
            (stepConfig as? MakeglossariesStepConfig)?.commandLine
                ?.takeIf(String::isNotBlank)
                ?: "makeglossaries ${context.mainFile.nameWithoutExtension}"
        },
    )
}
