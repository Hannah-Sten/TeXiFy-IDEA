package nl.hannahsten.texifyidea.run.latexmk

import nl.hannahsten.texifyidea.run.compiler.LatexCompiler
import nl.hannahsten.texifyidea.run.latex.LatexmkCompileStepOptions
import nl.hannahsten.texifyidea.run.latex.LatexRunConfiguration
import nl.hannahsten.texifyidea.run.latex.LatexRunSessionState
import nl.hannahsten.texifyidea.run.latex.LatexRunConfigurationStaticSupport
import nl.hannahsten.texifyidea.run.latex.LatexmkModeService

/**
 * Resolve whether the effective engine of a run configuration supports Unicode natively.
 *
 * Returns:
 * - true: engine is known to support Unicode natively (LuaLaTeX/XeLaTeX)
 * - false: engine is known not to support it natively (pdfLaTeX/LaTeX)
 * - null: unknown (e.g. custom command)
 */
fun unicodeEngineCompatibility(runConfig: LatexRunConfiguration?): Boolean? = when (runConfig?.activeCompiler()) {
    LatexCompiler.LATEXMK -> {
        val step = runConfig.activeCompileStep() as? LatexmkCompileStepOptions ?: return null
        val session = LatexRunSessionState(
            resolvedMainFile = LatexRunConfigurationStaticSupport.resolveMainFile(runConfig),
        )
        when (LatexmkModeService.effectiveCompileMode(runConfig, session, step)) {
            LatexmkCompileMode.LUALATEX_PDF, LatexmkCompileMode.XELATEX_PDF, LatexmkCompileMode.XELATEX_XDV -> true
            LatexmkCompileMode.AUTO, LatexmkCompileMode.PDFLATEX_PDF, LatexmkCompileMode.LATEX_DVI, LatexmkCompileMode.LATEX_PS -> false
            LatexmkCompileMode.CUSTOM -> null
        }
    }
    LatexCompiler.LUALATEX, LatexCompiler.XELATEX -> true
    LatexCompiler.PDFLATEX -> false
    else -> null
}
