package nl.hannahsten.texifyidea.run.latex.step

import nl.hannahsten.texifyidea.run.latex.LatexStepType

internal object LatexStepPresentation {

    private val descriptions: Map<String, String> = mapOf(
        LatexStepType.LATEX_COMPILE to "Compile LaTeX",
        LatexStepType.LATEXMK_COMPILE to "Compile with latexmk",
        LatexStepType.EXTERNAL_TOOL to "Run external tool",
        LatexStepType.MAKEINDEX to "Run makeindex",
        LatexStepType.BIBTEX to "Run bibliography",
        LatexStepType.PYTHONTEX to "Run pythontex",
        LatexStepType.MAKEGLOSSARIES to "Run makeglossaries",
        LatexStepType.XINDY to "Run xindy",
        LatexStepType.PDF_VIEWER to "Open PDF viewer",
        "legacy-external-tool" to "Run external tool",
        "legacy-makeindex" to "Run makeindex",
        "legacy-bibtex" to "Run bibliography",
        "pythontex-command" to "Run pythontex",
        "makeglossaries-command" to "Run makeglossaries",
        "xindy-command" to "Run xindy",
    )

    fun displayName(type: String): String = descriptions[type] ?: "Step: $type"
}
