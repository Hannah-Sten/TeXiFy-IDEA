package nl.hannahsten.texifyidea.run.latex.step

internal object LatexStepPresentation {

    private val descriptions: Map<String, String> = mapOf(
        "latex-compile" to "Compile LaTeX",
        "latexmk-compile" to "Compile with latexmk",
        "legacy-external-tool" to "Run external tool",
        "legacy-makeindex" to "Run makeindex",
        "legacy-bibtex" to "Run bibliography",
        "pythontex-command" to "Run pythontex",
        "makeglossaries-command" to "Run makeglossaries",
        "xindy-command" to "Run xindy",
        "pdf-viewer" to "Open PDF viewer",
    )

    fun displayName(type: String): String = descriptions[type] ?: "Step: $type"
}
