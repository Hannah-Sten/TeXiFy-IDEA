package nl.hannahsten.texifyidea.run.latex.step

import nl.hannahsten.texifyidea.run.latex.LatexStepConfig
import nl.hannahsten.texifyidea.run.latex.LatexStepType
import nl.hannahsten.texifyidea.run.latex.PythontexStepConfig

internal object PythontexCommandRunStepProvider : LatexRunStepProvider {

    override val type: String = LatexStepType.PYTHONTEX

    override val aliases: Set<String> = setOf(
        type,
        "pythontex-command",
        "pythontex",
    )

    override fun create(stepConfig: LatexStepConfig): LatexRunStep = CommandLineRunStep(
        configId = stepConfig.id,
        id = type,
        commandLineSupplier = { context ->
            (stepConfig as? PythontexStepConfig)?.commandLine
                ?.takeIf(String::isNotBlank)
                ?: "pythontex ${context.mainFile.nameWithoutExtension}"
        },
    )
}
