package nl.hannahsten.texifyidea.run.latex.step

import com.intellij.openapi.application.ReadAction
import com.intellij.openapi.vfs.VirtualFile
import nl.hannahsten.texifyidea.lang.LatexLib
import nl.hannahsten.texifyidea.run.latex.*
import nl.hannahsten.texifyidea.util.files.psiFile
import nl.hannahsten.texifyidea.util.includedPackagesInFileset
import nl.hannahsten.texifyidea.util.parser.hasBibliography
import nl.hannahsten.texifyidea.util.parser.usesBiber

internal object LatexRunStepAutoInference {

    private val compileTypes = setOf(LatexStepType.LATEX_COMPILE, LatexStepType.LATEXMK_COMPILE)

    fun augmentSteps(
        runConfig: LatexRunConfiguration,
        mainFile: VirtualFile,
        baseSteps: List<LatexStepRunConfigurationOptions>,
    ): List<LatexStepRunConfigurationOptions> {
        if (baseSteps.isEmpty()) {
            return baseSteps
        }

        val steps = baseSteps.map { it.deepCopy() }.toMutableList()
        val inferred = inferRequiredAuxiliarySteps(runConfig, mainFile, steps)
        if (inferred.isEmpty()) {
            return steps
        }

        val insertIndex = preferredAuxInsertIndex(steps)
        steps.addAll(insertIndex, inferred)
        ensureCompileAfterAuxSteps(steps, insertIndex + inferred.size - 1)

        return steps
    }

    private fun inferRequiredAuxiliarySteps(
        runConfig: LatexRunConfiguration,
        mainFile: VirtualFile,
        steps: List<LatexStepRunConfigurationOptions>,
    ): List<LatexStepRunConfigurationOptions> {
        val inferred = mutableListOf<LatexStepRunConfigurationOptions>()
        val mainFileText = ReadAction.compute<String, RuntimeException> {
            mainFile.psiFile(runConfig.project)?.text
                ?: runCatching { String(mainFile.contentsToByteArray()) }.getOrDefault("")
        }
        val packageNamesInText = extractUsedPackageNames(mainFileText)
        val usedPackages = ReadAction.compute<Set<LatexLib>, RuntimeException> {
            mainFile.psiFile(runConfig.project)?.includedPackagesInFileset() ?: emptySet()
        }

        if (shouldAddBibliographyStep(runConfig, mainFile, steps, usedPackages, mainFileText, packageNamesInText)) {
            inferred += BibtexStepOptions()
        }
        if (shouldAddPythontexStep(steps, usedPackages, packageNamesInText)) {
            inferred += PythontexStepOptions()
        }
        if (shouldAddMakeglossariesStep(steps, usedPackages, packageNamesInText)) {
            inferred += MakeglossariesStepOptions()
        }
        if (shouldAddXindyStep(steps, mainFileText)) {
            inferred += XindyStepOptions()
        }

        return inferred
    }

    private fun shouldAddBibliographyStep(
        runConfig: LatexRunConfiguration,
        mainFile: VirtualFile,
        steps: List<LatexStepRunConfigurationOptions>,
        usedPackages: Set<LatexLib>,
        mainFileText: String,
        packageNamesInText: Set<String>,
    ): Boolean {
        if (steps.any { it.type == LatexStepType.BIBTEX } || steps.any { it.type == LatexStepType.LATEXMK_COMPILE }) {
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

        return needsBibliographyFromPsi || needsBibliographyFromText
    }

    private fun shouldAddPythontexStep(
        steps: List<LatexStepRunConfigurationOptions>,
        usedPackages: Set<LatexLib>,
        packageNamesInText: Set<String>,
    ): Boolean {
        if (steps.any { it.type == LatexStepType.PYTHONTEX || it.type == LatexStepType.EXTERNAL_TOOL }) {
            return false
        }
        return LatexLib.PYTHONTEX in usedPackages || "pythontex" in packageNamesInText
    }

    private fun shouldAddMakeglossariesStep(
        steps: List<LatexStepRunConfigurationOptions>,
        usedPackages: Set<LatexLib>,
        packageNamesInText: Set<String>,
    ): Boolean {
        if (steps.any { it.type == LatexStepType.LATEXMK_COMPILE }) {
            return false
        }
        if (steps.any { it.type == LatexStepType.MAKEINDEX || it.type == LatexStepType.MAKEGLOSSARIES }) {
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
        steps: List<LatexStepRunConfigurationOptions>,
        mainFileText: String,
    ): Boolean {
        if (steps.any { it.type == LatexStepType.LATEXMK_COMPILE }) {
            return false
        }
        if (steps.any { it.type == LatexStepType.MAKEINDEX || it.type == LatexStepType.XINDY }) {
            return false
        }

        return mainFileText.contains("xindy") || mainFileText.contains("texindy")
    }

    private fun preferredAuxInsertIndex(steps: List<LatexStepRunConfigurationOptions>): Int {
        val firstCompileIndex = steps.indexOfFirst { it.type in compileTypes }
        val viewerIndex = steps.indexOfFirst { it.type == LatexStepType.PDF_VIEWER }.let { if (it < 0) steps.size else it }
        return if (firstCompileIndex >= 0) {
            (firstCompileIndex + 1).coerceAtMost(viewerIndex)
        }
        else {
            viewerIndex
        }
    }

    private fun ensureCompileAfterAuxSteps(steps: MutableList<LatexStepRunConfigurationOptions>, lastInsertedIndex: Int) {
        val hasCompileAfter = steps.withIndex().any { (index, step) ->
            index > lastInsertedIndex && step.type in compileTypes
        }
        if (hasCompileAfter) {
            return
        }

        val followUpCompile = steps.firstOrNull { it.type in compileTypes }?.deepCopy() ?: return
        val beforeViewer = steps.indexOfFirst { it.type == LatexStepType.PDF_VIEWER }.let { if (it < 0) steps.size else it }
        steps.add(beforeViewer, followUpCompile)
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
