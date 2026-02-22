package nl.hannahsten.texifyidea.run.latex.step

internal object PdfViewerRunStepProvider : LatexRunStepProvider {

    override val type: String = "pdf-viewer"

    override val aliases: Set<String> = setOf(
        type,
        "open-pdf",
        "open-pdf-viewer",
    )

    override fun create(spec: LatexRunStepSpec): LatexRunStep = PdfViewerRunStep()
}
