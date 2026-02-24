package nl.hannahsten.texifyidea.run.latex.step

import nl.hannahsten.texifyidea.run.latex.ExternalToolStepOptions
import nl.hannahsten.texifyidea.run.latex.LatexStepRunConfigurationOptions
import nl.hannahsten.texifyidea.run.latex.LatexStepType

internal object ExternalToolRunStepProvider : LatexRunStepProvider {

    override val type: String = LatexStepType.EXTERNAL_TOOL

    override val aliases: Set<String> = setOf(
        type,
        "commandline",
    )

    override fun create(stepConfig: LatexStepRunConfigurationOptions): LatexRunStep = ExternalToolRunStep(
        stepConfig as? ExternalToolStepOptions
            ?: error("Expected ExternalToolStepOptions for $type, but got ${stepConfig::class.simpleName}")
    )
}
