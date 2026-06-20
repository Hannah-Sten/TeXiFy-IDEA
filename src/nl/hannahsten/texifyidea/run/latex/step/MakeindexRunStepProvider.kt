package nl.hannahsten.texifyidea.run.latex.step

import nl.hannahsten.texifyidea.run.latex.LatexStepRunConfigurationOptions
import nl.hannahsten.texifyidea.run.latex.LatexStepType
import nl.hannahsten.texifyidea.run.latex.MakeindexStepOptions

internal object MakeindexRunStepProvider : LatexRunStepProvider {

    override val type: String = LatexStepType.MAKEINDEX

    override val aliases: Set<String> = setOf(
        type,
        "index",
        "makeindex-step",
    )

    override fun create(stepConfig: LatexStepRunConfigurationOptions): LatexRunStep = MakeindexRunStep(
        stepConfig as? MakeindexStepOptions
            ?: error("Expected MakeindexStepOptions for $type, but got ${stepConfig::class.simpleName}")
    )
}
