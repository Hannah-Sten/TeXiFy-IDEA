package nl.hannahsten.texifyidea.run.latexmk

import nl.hannahsten.texifyidea.index.projectstructure.pathOrNull
import nl.hannahsten.texifyidea.run.compiler.LatexCompiler
import nl.hannahsten.texifyidea.run.latex.LatexCompileStepOptions
import nl.hannahsten.texifyidea.run.latex.LatexRunConfiguration
import nl.hannahsten.texifyidea.run.latex.LatexRunConfigurationStaticSupport
import nl.hannahsten.texifyidea.run.latex.LatexRunSessionState
import nl.hannahsten.texifyidea.run.latex.LatexmkCompileStepOptions
import nl.hannahsten.texifyidea.run.latex.step.LatexmkCompileRunStep

/**
 * Resolve whether the effective engine of a run configuration supports Unicode natively.
 *
 * Returns:
 * - true: engine is known to support Unicode natively (LuaLaTeX/XeLaTeX)
 * - false: engine is known not to support it natively (pdfLaTeX/LaTeX)
 * - null: unknown (e.g. custom command)
 */
fun unicodeEngineCompatibility(runConfig: LatexRunConfiguration?): Boolean? {
    runConfig ?: return null
    return when (val step = runConfig.primaryCompileStep()) {
        is LatexmkCompileStepOptions -> {
            val effectiveMode = when (step.latexmkCompileMode) {
                LatexmkCompileMode.AUTO -> runCatching {
                    val session = compileModeSession(runConfig) ?: return@runCatching LatexmkCompileMode.PDFLATEX_PDF
                    LatexmkCompileRunStep.effectiveCompileMode(runConfig, session, step)
                }.getOrElse { LatexmkCompileMode.PDFLATEX_PDF }
                else -> step.latexmkCompileMode
            }
            when (effectiveMode) {
                LatexmkCompileMode.LUALATEX_PDF, LatexmkCompileMode.XELATEX_PDF, LatexmkCompileMode.XELATEX_XDV -> true
                LatexmkCompileMode.AUTO, LatexmkCompileMode.PDFLATEX_PDF, LatexmkCompileMode.LATEX_DVI, LatexmkCompileMode.LATEX_PS -> false
                LatexmkCompileMode.CUSTOM -> null
            }
        }
        is LatexCompileStepOptions -> when (step.compiler) {
            LatexCompiler.LUALATEX, LatexCompiler.XELATEX -> true
            LatexCompiler.PDFLATEX -> false
            else -> null
        }
        else -> null
    }
}

private fun compileModeSession(runConfig: LatexRunConfiguration): LatexRunSessionState? {
    val mainFile = LatexRunConfigurationStaticSupport.resolveMainFile(runConfig) ?: return null
    val workingDirectory = pathOrNull(mainFile.parent.path) ?: return null
    return LatexRunSessionState(
        project = runConfig.project,
        mainFile = mainFile,
        outputDir = mainFile.parent,
        workingDirectory = workingDirectory,
        distributionType = runConfig.getLatexDistributionType(),
        usesDefaultWorkingDirectory = runConfig.hasDefaultWorkingDirectory(),
        latexSdk = runConfig.getLatexSdk(),
        auxDir = null,
        psiFile = null,
    )
}
