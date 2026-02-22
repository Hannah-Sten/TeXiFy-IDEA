package nl.hannahsten.texifyidea.run.latex.step

internal data class LatexRunStepPlan(
    val steps: List<LatexRunStep>,
    val unsupportedTypes: List<String>,
)

internal object LatexRunStepPlanBuilder {

    private val latexAliases = setOf("latex-compile", "compile-latex")
    private val viewerAliases = setOf("pdf-viewer", "open-pdf", "open-pdf-viewer")

    fun build(stepTypes: List<String>): LatexRunStepPlan {
        val steps = mutableListOf<LatexRunStep>()
        val unsupported = mutableListOf<String>()

        for (raw in stepTypes) {
            val type = raw.trim().lowercase()
            val step = when {
                type in latexAliases -> LatexCompileRunStep()
                type in viewerAliases -> PdfViewerRunStep()
                else -> null
            }
            if (step == null) {
                unsupported += raw
            }
            else {
                steps += step
            }
        }

        return LatexRunStepPlan(steps = steps, unsupportedTypes = unsupported)
    }
}
