package nl.hannahsten.texifyidea.run.latex.step

import nl.hannahsten.texifyidea.run.latex.LatexStepRunConfigurationOptions
import nl.hannahsten.texifyidea.run.latex.LatexStepType
import nl.hannahsten.texifyidea.run.latex.MakeglossariesStepOptions

internal object MakeglossariesCommandRunStepProvider : LatexRunStepProvider {

    override val type: String = LatexStepType.MAKEGLOSSARIES

    override val aliases: Set<String> = setOf(
        type,
        "makeglossaries-command",
        "makeglossaries",
    )

    override fun create(stepConfig: LatexStepRunConfigurationOptions): LatexRunStep = CommandLineRunStep(
        configId = stepConfig.id,
        id = type,
        commandLineSupplier = { context ->
            (stepConfig as? MakeglossariesStepOptions)?.commandLine
                ?.takeIf(String::isNotBlank)
                ?: "makeglossaries ${context.mainFile.nameWithoutExtension}"
        },
    )
}
