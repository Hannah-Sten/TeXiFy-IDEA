package nl.hannahsten.texifyidea.run.latexmk

enum class LatexmkEngineMode(private val displayName: String) {
    PDFLATEX("pdfLaTeX"),
    XELATEX("XeLaTeX"),
    LUALATEX("LuaLaTeX"),
    LATEX("LaTeX"),
    CUSTOM_COMMAND("Custom command");

    override fun toString(): String = displayName
}

enum class LatexmkCitationTool(private val displayName: String) {
    AUTO("Auto"),
    BIBTEX("BibTeX"),
    BIBER("Biber"),
    DISABLED("Disabled");

    override fun toString(): String = displayName
}

enum class LatexmkOutputFormat(private val displayName: String, val extension: String) {
    DEFAULT("Default", "pdf"),
    PDF("PDF", "pdf"),
    DVI("DVI", "dvi"),
    PS("PostScript", "ps"),
    XDV("XDV", "xdv");

    override fun toString(): String = displayName
}
