package nl.hannahsten.texifyidea.run.latex.step

import com.intellij.openapi.application.ReadAction
import com.intellij.openapi.project.DumbService
import com.intellij.openapi.vfs.VirtualFile
import nl.hannahsten.texifyidea.lang.LatexLib
import nl.hannahsten.texifyidea.run.latex.LatexRunConfiguration
import nl.hannahsten.texifyidea.util.files.psiFile
import nl.hannahsten.texifyidea.util.includedPackagesInFileset
import nl.hannahsten.texifyidea.util.parser.hasBibliography
import nl.hannahsten.texifyidea.util.parser.usesBiber

internal object LatexRunStepAutoInference {

    private const val LATEX_COMPILE = "latex-compile"
    private const val PDF_VIEWER = "pdf-viewer"
    private const val LEGACY_BIBTEX = "legacy-bibtex"
    private const val LEGACY_MAKEINDEX = "legacy-makeindex"
    private const val LEGACY_EXTERNAL_TOOL = "legacy-external-tool"
    private const val PYTHONTEX_COMMAND = "pythontex-command"
    private const val MAKEGLOSSARIES_COMMAND = "makeglossaries-command"
    private const val XINDY_COMMAND = "xindy-command"

    fun augmentStepTypes(
        runConfig: LatexRunConfiguration,
        mainFile: VirtualFile,
        baseStepTypes: List<String>,
    ): List<String> {
        if (baseStepTypes.isEmpty()) {
            return baseStepTypes
        }

        val stepTypes = baseStepTypes.toMutableList()
        val inferred = inferRequiredAuxiliarySteps(runConfig, mainFile, stepTypes)
        if (inferred.isEmpty()) {
            return stepTypes
        }

        val insertIndex = preferredAuxInsertIndex(stepTypes)
        stepTypes.addAll(insertIndex, inferred)
        ensureCompileAfterAuxSteps(stepTypes, insertIndex + inferred.size - 1)

        return stepTypes
    }

    private fun inferRequiredAuxiliarySteps(
        runConfig: LatexRunConfiguration,
        mainFile: VirtualFile,
        stepTypes: List<String>,
    ): List<String> {
        val inferred = mutableListOf<String>()
        val mainFileText = ReadAction.compute<String, RuntimeException> {
            mainFile.psiFile(runConfig.project)?.text
                ?: runCatching { String(mainFile.contentsToByteArray()) }.getOrDefault("")
        }
        val packageNamesInText = extractUsedPackageNames(mainFileText)
        val usedPackages = ReadAction.compute<Set<LatexLib>, RuntimeException> {
            mainFile.psiFile(runConfig.project)?.includedPackagesInFileset() ?: emptySet()
        }

        if (shouldAddBibliographyStep(runConfig, mainFile, stepTypes, usedPackages, mainFileText, packageNamesInText)) {
            inferred += LEGACY_BIBTEX
        }
        if (shouldAddPythontexStep(stepTypes, usedPackages, packageNamesInText)) {
            inferred += PYTHONTEX_COMMAND
        }
        if (shouldAddMakeglossariesStep(runConfig, stepTypes, usedPackages, packageNamesInText)) {
            inferred += MAKEGLOSSARIES_COMMAND
        }
        if (shouldAddXindyStep(runConfig, stepTypes, mainFileText)) {
            inferred += XINDY_COMMAND
        }

        return inferred
    }

    private fun shouldAddBibliographyStep(
        runConfig: LatexRunConfiguration,
        mainFile: VirtualFile,
        stepTypes: List<String>,
        usedPackages: Set<LatexLib>,
        mainFileText: String,
        packageNamesInText: Set<String>,
    ): Boolean {
        if (LEGACY_BIBTEX in stepTypes || runConfig.compiler?.includesBibtex == true) {
            return false
        }
        if (LatexLib.CITATION_STYLE_LANGUAGE in usedPackages || "citation-style-language" in packageNamesInText) {
            return false
        }

        val needsBibliographyFromPsi = ReadAction.compute<Boolean, RuntimeException> {
            val psi = mainFile.psiFile(runConfig.project) ?: return@compute false
            psi.hasBibliography() || psi.usesBiber()
        }
        val needsBibliographyFromText = mainFileText.contains("\\bibliography")
            ||
            mainFileText.contains("\\printbibliography")
            ||
            mainFileText.contains("\\addbibresource")
        val needsBibliographyStep = needsBibliographyFromPsi || needsBibliographyFromText
        if (!needsBibliographyStep) {
            return false
        }

        if (runConfig.bibRunConfigs.isEmpty() && !DumbService.getInstance(runConfig.project).isDumb) {
            ReadAction.run<RuntimeException> {
                runConfig.generateBibRunConfig()
            }
        }

        return runConfig.bibRunConfigs.isNotEmpty() || needsBibliographyStep
    }

    private fun shouldAddPythontexStep(
        stepTypes: List<String>,
        usedPackages: Set<LatexLib>,
        packageNamesInText: Set<String>,
    ): Boolean {
        if (PYTHONTEX_COMMAND in stepTypes || LEGACY_EXTERNAL_TOOL in stepTypes) {
            return false
        }
        return LatexLib.PYTHONTEX in usedPackages || "pythontex" in packageNamesInText
    }

    private fun shouldAddMakeglossariesStep(
        runConfig: LatexRunConfiguration,
        stepTypes: List<String>,
        usedPackages: Set<LatexLib>,
        packageNamesInText: Set<String>,
    ): Boolean {
        if (runConfig.compiler?.includesMakeindex == true) {
            return false
        }
        if (LEGACY_MAKEINDEX in stepTypes || MAKEGLOSSARIES_COMMAND in stepTypes) {
            return false
        }

        return LatexLib.GLOSSARIES in usedPackages
            ||
            LatexLib.GLOSSARIESEXTRA in usedPackages
            ||
            "glossaries" in packageNamesInText
            ||
            "glossaries-extra" in packageNamesInText
    }

    private fun shouldAddXindyStep(
        runConfig: LatexRunConfiguration,
        stepTypes: List<String>,
        mainFileText: String,
    ): Boolean {
        if (runConfig.compiler?.includesMakeindex == true) {
            return false
        }
        if (LEGACY_MAKEINDEX in stepTypes || XINDY_COMMAND in stepTypes) {
            return false
        }

        return mainFileText.contains("xindy") || mainFileText.contains("texindy")
    }

    private fun preferredAuxInsertIndex(stepTypes: List<String>): Int {
        val firstCompileIndex = stepTypes.indexOfFirst { it == LATEX_COMPILE }
        val viewerIndex = stepTypes.indexOfFirst { it == PDF_VIEWER }.let { if (it < 0) stepTypes.size else it }
        return if (firstCompileIndex >= 0) {
            (firstCompileIndex + 1).coerceAtMost(viewerIndex)
        }
        else {
            viewerIndex
        }
    }

    private fun ensureCompileAfterAuxSteps(stepTypes: MutableList<String>, lastInsertedIndex: Int) {
        val hasCompileBeforeViewer = stepTypes.withIndex().any { (index, type) ->
            index > lastInsertedIndex && type == LATEX_COMPILE
        }
        if (hasCompileBeforeViewer) {
            return
        }

        if (stepTypes.none { it == LATEX_COMPILE }) {
            return
        }

        val beforeViewer = stepTypes.indexOfFirst { it == PDF_VIEWER }.let { if (it < 0) stepTypes.size else it }
        stepTypes.add(beforeViewer, LATEX_COMPILE)
    }

    private fun extractUsedPackageNames(mainFileText: String): Set<String> {
        val commandRegex = Regex("""\\usepackage(?:\[[^\]]*])?\{([^}]*)}""")
        return commandRegex.findAll(mainFileText)
            .flatMap { match -> match.groupValues[1].split(',').asSequence() }
            .map { it.trim() }
            .filter { it.isNotEmpty() }
            .toSet()
    }
}
