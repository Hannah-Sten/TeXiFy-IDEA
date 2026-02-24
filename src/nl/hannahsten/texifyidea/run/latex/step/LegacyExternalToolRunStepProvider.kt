package nl.hannahsten.texifyidea.run.latex.step

import nl.hannahsten.texifyidea.run.latex.ExternalToolStepConfig
import nl.hannahsten.texifyidea.run.latex.LatexStepConfig
import nl.hannahsten.texifyidea.run.latex.LatexStepType

internal object LegacyExternalToolRunStepProvider : LatexRunStepProvider {

    override val type: String = LatexStepType.EXTERNAL_TOOL

    override val aliases: Set<String> = setOf(
        type,
        "legacy-external-tool",
        "external-tool",
        "commandline",
    )

    override fun create(stepConfig: LatexStepConfig): LatexRunStep = ExternalToolRunStep(
        stepConfig as? ExternalToolStepConfig
            ?: error("Expected ExternalToolStepConfig for $type, but got ${stepConfig::class.simpleName}")
    )
}
