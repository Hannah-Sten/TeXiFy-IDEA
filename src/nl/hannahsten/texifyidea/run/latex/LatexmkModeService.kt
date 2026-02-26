package nl.hannahsten.texifyidea.run.latex

import com.intellij.openapi.application.ReadAction
import com.intellij.psi.PsiManager
import nl.hannahsten.texifyidea.lang.magic.DefaultMagicKeys
import nl.hannahsten.texifyidea.lang.magic.allParentMagicComments
import nl.hannahsten.texifyidea.run.latexmk.LatexmkCompileMode
import nl.hannahsten.texifyidea.run.latexmk.buildLatexmkStructuredArguments
import nl.hannahsten.texifyidea.run.latexmk.compileModeFromMagicCommand
import nl.hannahsten.texifyidea.run.latexmk.preferredCompileModeForPackages
import nl.hannahsten.texifyidea.util.LatexmkRcFileFinder
import nl.hannahsten.texifyidea.util.includedPackagesInFileset

internal object LatexmkModeService {

    fun buildArguments(
        runConfig: LatexRunConfiguration,
        session: LatexRunSessionState,
        step: LatexmkCompileStepOptions,
        effectiveCompileModeOverride: LatexmkCompileMode? = null
    ): String {
        val hasRcFile = LatexmkRcFileFinder.hasLatexmkRc(step.compilerArguments, session.resolvedWorkingDirectory)
        val effectiveCompileMode = effectiveCompileModeOverride ?: effectiveCompileMode(runConfig, session, step)
        return buildLatexmkStructuredArguments(
            hasRcFile = hasRcFile,
            compileMode = effectiveCompileMode,
            citationTool = step.latexmkCitationTool,
            customEngineCommand = step.latexmkCustomEngineCommand,
            extraArguments = step.latexmkExtraArguments,
        )
    }

    fun effectiveCompileMode(runConfig: LatexRunConfiguration, session: LatexRunSessionState, step: LatexmkCompileStepOptions): LatexmkCompileMode {
        if (step.latexmkCompileMode != LatexmkCompileMode.AUTO) {
            return step.latexmkCompileMode
        }

        return ReadAction.compute<LatexmkCompileMode, RuntimeException> {
            val mainFile = session.resolvedMainFile
            val psi = mainFile?.let { PsiManager.getInstance(runConfig.project).findFile(it) } ?: session.psiFile?.element
            val magicComments = psi?.allParentMagicComments()
            val magicMode = compileModeFromMagicCommand(
                magicComments?.value(DefaultMagicKeys.COMPILER) ?: magicComments?.value(DefaultMagicKeys.PROGRAM)
            )
            val packageMode = psi?.let { psiFile ->
                preferredCompileModeForPackages(psiFile.includedPackagesInFileset())
            }
            magicMode ?: packageMode ?: LatexmkCompileMode.PDFLATEX_PDF
        }
    }
}
