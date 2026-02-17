package nl.hannahsten.texifyidea.run.latexmk

import nl.hannahsten.texifyidea.run.compiler.LatexCompiler
import nl.hannahsten.texifyidea.run.latex.LatexCompilationRunConfiguration
import nl.hannahsten.texifyidea.run.latex.LatexRunConfiguration

/**
 * Resolve whether the effective engine of a run configuration supports Unicode natively.
 *
 * Returns:
 * - true: engine is known to support Unicode natively (LuaLaTeX/XeLaTeX)
 * - false: engine is known not to support it natively (pdfLaTeX/LaTeX)
 * - null: unknown (e.g. custom command)
 */
fun unicodeEngineCompatibility(runConfig: LatexCompilationRunConfiguration?): Boolean? = when (runConfig) {
    is LatexmkRunConfiguration -> when (runConfig.compileMode) {
        LatexmkCompileMode.LUALATEX_PDF, LatexmkCompileMode.XELATEX_PDF, LatexmkCompileMode.XELATEX_XDV -> true
        LatexmkCompileMode.PDFLATEX_PDF, LatexmkCompileMode.LATEX_DVI, LatexmkCompileMode.LATEX_PS -> false
        LatexmkCompileMode.CUSTOM -> null
    }
    is LatexRunConfiguration -> when (runConfig.compiler) {
        LatexCompiler.LUALATEX, LatexCompiler.XELATEX -> true
        LatexCompiler.PDFLATEX -> false
        else -> null
    }
    else -> null
}
