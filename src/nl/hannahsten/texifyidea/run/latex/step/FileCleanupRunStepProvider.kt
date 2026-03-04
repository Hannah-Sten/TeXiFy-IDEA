package nl.hannahsten.texifyidea.run.latex.step

import nl.hannahsten.texifyidea.run.latex.FileCleanupStepOptions
import nl.hannahsten.texifyidea.run.latex.LatexStepRunConfigurationOptions
import nl.hannahsten.texifyidea.run.latex.LatexStepType

internal object FileCleanupRunStepProvider : LatexRunStepProvider {

    override val type: String = LatexStepType.FILE_CLEANUP

    override val aliases: Set<String> = setOf(
        type,
        "cleanup",
        "file-cleanup-step",
    )

    override fun create(stepConfig: LatexStepRunConfigurationOptions): LatexRunStep = FileCleanupRunStep(
        stepConfig as? FileCleanupStepOptions
            ?: error("Expected FileCleanupStepOptions for $type, but got ${stepConfig::class.simpleName}")
    )
}
