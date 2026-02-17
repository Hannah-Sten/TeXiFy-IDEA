package nl.hannahsten.texifyidea.run.latexmk

enum class LatexmkCompileMode(private val displayName: String, val extension: String) {
    PDFLATEX_PDF("pdfLaTeX (PDF)", "pdf"),
    LUALATEX_PDF("LuaLaTeX (PDF)", "pdf"),
    XELATEX_PDF("XeLaTeX (PDF)", "pdf"),
    LATEX_DVI("LaTeX (DVI)", "dvi"),
    XELATEX_XDV("XeLaTeX (XDV)", "xdv"),
    LATEX_PS("LaTeX (PS)", "ps"),
    CUSTOM("Custom command", "pdf");

    override fun toString(): String = displayName
}

enum class LatexmkCitationTool(private val displayName: String) {
    AUTO("Auto"),
    BIBTEX("BibTeX"),
    BIBER("Biber"),
    DISABLED("Disabled");

    override fun toString(): String = displayName
}
