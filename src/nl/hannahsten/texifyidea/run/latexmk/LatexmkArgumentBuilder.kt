package nl.hannahsten.texifyidea.run.latexmk

import com.intellij.util.execution.ParametersListUtil

internal fun buildLatexmkStructuredArguments(
    hasRcFile: Boolean,
    compileMode: LatexmkCompileMode,
    citationTool: LatexmkCitationTool,
    customEngineCommand: String?,
    extraArguments: String?,
): String {
    val arguments = mutableListOf<String>()

    val hasExplicitStructuredOptions =
        compileMode != LatexmkCompileMode.PDFLATEX_PDF ||
            citationTool != LatexmkCitationTool.AUTO ||
            (compileMode == LatexmkCompileMode.CUSTOM && !customEngineCommand.isNullOrBlank())

    if (!hasRcFile || hasExplicitStructuredOptions) {
        arguments += compileMode.toLatexmkFlags(customEngineCommand)
        arguments += citationTool.toLatexmkFlags()
    }

    extraArguments?.let { arguments += ParametersListUtil.parse(it) }
    return ParametersListUtil.join(arguments)
}

private fun LatexmkCompileMode.toLatexmkFlags(customEngineCommand: String?): List<String> = when (this) {
    LatexmkCompileMode.AUTO -> listOf("-pdf")
    LatexmkCompileMode.PDFLATEX_PDF -> listOf("-pdf")
    LatexmkCompileMode.LUALATEX_PDF -> listOf("-lualatex")
    LatexmkCompileMode.XELATEX_PDF -> listOf("-xelatex")
    LatexmkCompileMode.LATEX_DVI -> listOf("-latex", "-dvi")
    LatexmkCompileMode.XELATEX_XDV -> listOf("-xelatex", "-xdv")
    LatexmkCompileMode.LATEX_PS -> listOf("-latex", "-ps")
    LatexmkCompileMode.CUSTOM -> customEngineCommand?.let {
        val escaped = it.replace("\"", "\\\"")
        listOf("-pdflatex=\"$escaped\"")
    } ?: emptyList()
}

private fun LatexmkCitationTool.toLatexmkFlags(): List<String> = when (this) {
    LatexmkCitationTool.AUTO -> emptyList()
    LatexmkCitationTool.BIBTEX -> listOf("-bibtex")
    LatexmkCitationTool.BIBER -> listOf("-use-biber")
    LatexmkCitationTool.DISABLED -> listOf("-bibtex-")
}
