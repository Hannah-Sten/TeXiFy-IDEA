package nl.hannahsten.texifyidea.run.latex.step

import nl.hannahsten.texifyidea.run.latex.LatexStepConfig
import nl.hannahsten.texifyidea.run.latex.LatexStepType
import nl.hannahsten.texifyidea.run.latex.MakeindexStepConfig

internal object LegacyMakeindexRunStepProvider : LatexRunStepProvider {

    override val type: String = LatexStepType.MAKEINDEX

    override val aliases: Set<String> = setOf(
        type,
        "legacy-makeindex",
        "index",
        "makeindex-step",
    )

    override fun create(stepConfig: LatexStepConfig): LatexRunStep = MakeindexRunStep(
        stepConfig as? MakeindexStepConfig
            ?: error("Expected MakeindexStepConfig for $type, but got ${stepConfig::class.simpleName}")
    )
}
