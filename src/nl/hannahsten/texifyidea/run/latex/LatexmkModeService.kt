package nl.hannahsten.texifyidea.run.latex

import com.intellij.openapi.application.ReadAction
import com.intellij.psi.PsiManager
import nl.hannahsten.texifyidea.lang.LatexLib
import nl.hannahsten.texifyidea.lang.magic.DefaultMagicKeys
import nl.hannahsten.texifyidea.lang.magic.allParentMagicComments
import nl.hannahsten.texifyidea.lang.predefined.CommandNames
import nl.hannahsten.texifyidea.psi.nameWithSlash
import nl.hannahsten.texifyidea.psi.traverseCommands
import nl.hannahsten.texifyidea.run.latexmk.LatexmkCompileMode
import nl.hannahsten.texifyidea.run.latexmk.buildLatexmkStructuredArguments
import nl.hannahsten.texifyidea.run.latexmk.compileModeFromMagicCommand
import nl.hannahsten.texifyidea.run.latexmk.preferredCompileModeForPackages
import nl.hannahsten.texifyidea.util.LatexmkRcFileFinder
import nl.hannahsten.texifyidea.util.includedPackagesInFileset

internal class LatexmkModeService(private val runConfig: LatexRunConfiguration) {

    fun buildArguments(): String {
        val hasRcFile = LatexmkRcFileFinder.hasLatexmkRc(runConfig.compilerArguments, runConfig.getResolvedWorkingDirectory())
        val effectiveCompileMode = effectiveCompileMode()
        return buildLatexmkStructuredArguments(
            hasRcFile = hasRcFile,
            compileMode = effectiveCompileMode,
            citationTool = runConfig.latexmkCitationTool,
            customEngineCommand = runConfig.latexmkCustomEngineCommand,
            extraArguments = runConfig.latexmkExtraArguments,
        )
    }

    fun effectiveCompileMode(): LatexmkCompileMode {
        if (runConfig.latexmkCompileMode != LatexmkCompileMode.AUTO) {
            return runConfig.latexmkCompileMode
        }

        return ReadAction.compute<LatexmkCompileMode, RuntimeException> {
            val psi = runConfig.mainFile?.let { PsiManager.getInstance(runConfig.project).findFile(it) } ?: runConfig.psiFile?.element
            val magicComments = psi?.allParentMagicComments()
            val magicMode = compileModeFromMagicCommand(
                magicComments?.value(DefaultMagicKeys.COMPILER) ?: magicComments?.value(DefaultMagicKeys.PROGRAM)
            )
            val packageMode = psi?.let { psiFile ->
                val directLibraries = mutableSetOf<LatexLib>()
                psiFile.traverseCommands(4).forEach { command ->
                    when (command.nameWithSlash) {
                        CommandNames.USE_PACKAGE -> {
                            command.requiredParameterText(0)
                                ?.split(",")
                                ?.map { it.trim() }
                                ?.filter { it.isNotBlank() }
                                ?.forEach { directLibraries.add(LatexLib.Package(it)) }
                        }

                        CommandNames.DOCUMENT_CLASS -> {
                            command.requiredParameterText(0)
                                ?.trim()
                                ?.takeIf { it.isNotBlank() }
                                ?.let { directLibraries.add(LatexLib.Class(it)) }
                        }
                    }
                }

                preferredCompileModeForPackages(directLibraries)
                    ?: preferredCompileModeForPackages(psiFile.includedPackagesInFileset())
            }
            magicMode ?: packageMode ?: LatexmkCompileMode.PDFLATEX_PDF
        }
    }
}
