package nl.hannahsten.texifyidea.run.latexmk

import nl.hannahsten.texifyidea.TexifyBundle

enum class LatexmkCompileMode(private val messageKey: String, val extension: String) {
    AUTO("run.latexmk.compile.mode.auto", "pdf"),
    PDFLATEX_PDF("run.latexmk.compile.mode.pdflatex.pdf", "pdf"),
    LUALATEX_PDF("run.latexmk.compile.mode.lualatex.pdf", "pdf"),
    XELATEX_PDF("run.latexmk.compile.mode.xelatex.pdf", "pdf"),
    LATEX_DVI("run.latexmk.compile.mode.latex.dvi", "dvi"),
    XELATEX_XDV("run.latexmk.compile.mode.xelatex.xdv", "xdv"),
    LATEX_PS("run.latexmk.compile.mode.latex.ps", "ps"),
    CUSTOM("run.latexmk.compile.mode.custom.command", "pdf");

    override fun toString(): String = TexifyBundle.message(messageKey)
}

enum class LatexmkCitationTool(private val messageKey: String) {
    AUTO("run.latexmk.citation.tool.auto"),
    BIBTEX("run.latexmk.citation.tool.bibtex"),
    BIBER("run.latexmk.citation.tool.biber"),
    DISABLED("run.latexmk.citation.tool.disabled");

    override fun toString(): String = TexifyBundle.message(messageKey)
}
