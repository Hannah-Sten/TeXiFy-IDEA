package nl.hannahsten.texifyidea.run.latex.step

import nl.hannahsten.texifyidea.run.latex.LatexStepRunConfigurationOptions
import nl.hannahsten.texifyidea.run.latex.LatexStepType
import nl.hannahsten.texifyidea.run.latex.PdfViewerStepOptions

internal object PdfViewerRunStepProvider : LatexRunStepProvider {

    override val type: String = LatexStepType.PDF_VIEWER

    override val aliases: Set<String> = setOf(
        type,
        "open-pdf",
        "open-pdf-viewer",
    )

    override fun create(stepConfig: LatexStepRunConfigurationOptions): LatexRunStep = PdfViewerRunStep(
        stepConfig as? PdfViewerStepOptions
            ?: error("Expected PdfViewerStepOptions for $type, but got ${stepConfig::class.simpleName}")
    )
}
