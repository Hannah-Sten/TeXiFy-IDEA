package nl.hannahsten.texifyidea.run.latex.step

import nl.hannahsten.texifyidea.run.latex.LatexStepRunConfigurationOptions
import nl.hannahsten.texifyidea.run.latex.LatexStepType
import nl.hannahsten.texifyidea.run.latex.PythontexStepOptions

internal object PythontexCommandRunStepProvider : LatexRunStepProvider {

    override val type: String = LatexStepType.PYTHONTEX

    override val aliases: Set<String> = setOf(
        type,
        "pythontex-command",
        "pythontex",
    )

    override fun create(stepConfig: LatexStepRunConfigurationOptions): LatexRunStep = CommandLineRunStep(
        configId = stepConfig.id,
        id = type,
        commandLineSupplier = { context ->
            (stepConfig as? PythontexStepOptions)?.commandLine
                ?.takeIf(String::isNotBlank)
                ?: "pythontex ${context.mainFile.nameWithoutExtension}"
        },
    )
}
