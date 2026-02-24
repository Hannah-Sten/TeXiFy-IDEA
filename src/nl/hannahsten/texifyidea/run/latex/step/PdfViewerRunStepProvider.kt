package nl.hannahsten.texifyidea.run.latex.step

import nl.hannahsten.texifyidea.run.latex.LatexStepConfig
import nl.hannahsten.texifyidea.run.latex.LatexStepType
import nl.hannahsten.texifyidea.run.latex.PdfViewerStepConfig

internal object PdfViewerRunStepProvider : LatexRunStepProvider {

    override val type: String = LatexStepType.PDF_VIEWER

    override val aliases: Set<String> = setOf(
        type,
        "open-pdf",
        "open-pdf-viewer",
    )

    override fun create(stepConfig: LatexStepConfig): LatexRunStep = PdfViewerRunStep(
        stepConfig as? PdfViewerStepConfig
            ?: error("Expected PdfViewerStepConfig for $type, but got ${stepConfig::class.simpleName}")
    )
}
