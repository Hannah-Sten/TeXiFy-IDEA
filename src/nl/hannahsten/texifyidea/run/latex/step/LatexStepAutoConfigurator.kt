package nl.hannahsten.texifyidea.run.latex.step

import com.intellij.openapi.application.ReadAction
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiFile
import nl.hannahsten.texifyidea.index.NewCommandsIndex
import nl.hannahsten.texifyidea.lang.LatexLib
import nl.hannahsten.texifyidea.lang.magic.DefaultMagicKeys
import nl.hannahsten.texifyidea.lang.magic.allParentMagicComments
import nl.hannahsten.texifyidea.lang.predefined.CommandNames
import nl.hannahsten.texifyidea.run.compiler.LatexCompilePrograms
import nl.hannahsten.texifyidea.run.compiler.LatexCompiler
import nl.hannahsten.texifyidea.run.latex.BibtexStepOptions
import nl.hannahsten.texifyidea.run.latex.LatexCompileStepOptions
import nl.hannahsten.texifyidea.run.latex.LatexRunConfiguration
import nl.hannahsten.texifyidea.run.latex.LatexStepRunConfigurationOptions
import nl.hannahsten.texifyidea.run.latex.LatexStepType
import nl.hannahsten.texifyidea.run.latex.LatexmkCompileStepOptions
import nl.hannahsten.texifyidea.run.latex.MakeglossariesStepOptions
import nl.hannahsten.texifyidea.run.latex.PdfViewerStepOptions
import nl.hannahsten.texifyidea.run.latex.PythontexStepOptions
import nl.hannahsten.texifyidea.run.latex.defaultLatexmkSteps
import nl.hannahsten.texifyidea.run.latex.generateLatexStepId
import nl.hannahsten.texifyidea.run.latexmk.LatexmkCompileMode
import nl.hannahsten.texifyidea.util.files.findTectonicTomlFile
import nl.hannahsten.texifyidea.util.files.hasTectonicTomlFile
import nl.hannahsten.texifyidea.util.includedPackagesInFileset
import nl.hannahsten.texifyidea.util.parser.hasBibliography
import nl.hannahsten.texifyidea.util.parser.usesBiber
import java.nio.file.Path

internal object LatexStepAutoConfigurator {

    private val compileTypes = setOf(LatexStepType.LATEX_COMPILE, LatexStepType.LATEXMK_COMPILE)
    private val followUpCompileAnchorTypes = setOf(
        LatexStepType.BIBTEX,
        LatexStepType.MAKEINDEX,
        LatexStepType.PYTHONTEX,
        LatexStepType.MAKEGLOSSARIES,
        LatexStepType.XINDY,
    )

    fun configureTemplate(): List<LatexStepRunConfigurationOptions> = completeSteps(null, defaultLatexmkSteps())

    fun configureFromContext(
        runConfig: LatexRunConfiguration,
        contextPsiFile: PsiFile,
        mainVirtualFile: VirtualFile,
    ): List<LatexStepRunConfigurationOptions> {
        val baseSteps = if (runConfig.configOptions.steps.isEmpty()) {
            defaultLatexmkSteps()
        }
        else {
            runConfig.configOptions.steps.map { it.deepCopy() }.toMutableList()
        }

        val commandSpec = resolveCommandSpec(runConfig, contextPsiFile)
        if (commandSpec.isLatexmk) {
            val step = ensurePrimaryCompileStepLatexmk(baseSteps)
            step.compilerArguments = commandSpec.arguments
            step.latexmkCompileMode = LatexmkCompileMode.AUTO
        }
        else {
            val step = ensurePrimaryCompileStepClassic(baseSteps)
            step.compiler = commandSpec.compiler ?: LatexCompiler.PDFLATEX
            step.compilerArguments = commandSpec.arguments
        }

        runConfig.workingDirectory = resolveWorkingDirectory(commandSpec.compiler, mainVirtualFile)
        return completeSteps(contextPsiFile, baseSteps)
    }

    fun completeSteps(
        mainPsiFile: PsiFile?,
        baseSteps: List<LatexStepRunConfigurationOptions>,
    ): List<LatexStepRunConfigurationOptions> {
        val normalized = normalizeBaseSteps(baseSteps)
        val inferred = inferAuxiliarySteps(mainPsiFile, normalized)
        val withAux = insertInferredAuxSteps(normalized, inferred)
        return closePipeline(withAux)
    }

    private fun normalizeBaseSteps(
        baseSteps: List<LatexStepRunConfigurationOptions>,
    ): MutableList<LatexStepRunConfigurationOptions> = if (baseSteps.isEmpty()) {
        defaultLatexmkSteps()
    }
    else {
        baseSteps.map { it.deepCopy() }.toMutableList()
    }

    private fun inferAuxiliarySteps(
        mainPsiFile: PsiFile?,
        steps: List<LatexStepRunConfigurationOptions>,
    ): List<LatexStepRunConfigurationOptions> {
        if (mainPsiFile == null) {
            return emptyList()
        }

        return inferRequiredAuxiliarySteps(mainPsiFile, steps)
    }

    private fun insertInferredAuxSteps(
        steps: MutableList<LatexStepRunConfigurationOptions>,
        inferred: List<LatexStepRunConfigurationOptions>,
    ): MutableList<LatexStepRunConfigurationOptions> {
        if (inferred.isEmpty()) {
            return steps
        }

        val insertIndex = preferredAuxInsertIndex(steps)
        steps.addAll(insertIndex, inferred)
        return steps
    }

    private fun closePipeline(
        steps: MutableList<LatexStepRunConfigurationOptions>,
    ): MutableList<LatexStepRunConfigurationOptions> {
        if (steps.isEmpty()) {
            steps += defaultLatexmkSteps()
        }

        val firstCompile = steps.firstOrNull { it.type in compileTypes }
        when (firstCompile?.type) {
            LatexStepType.LATEXMK_COMPILE -> ensureViewerStep(steps)
            else -> ensureClassicCompileFlow(steps)
        }

        return steps
    }

    private fun ensureViewerStep(steps: MutableList<LatexStepRunConfigurationOptions>) {
        if (steps.none { it.type == LatexStepType.PDF_VIEWER }) {
            steps += PdfViewerStepOptions()
        }
    }

    private fun ensureClassicCompileFlow(steps: MutableList<LatexStepRunConfigurationOptions>) {
        if (steps.none { it.type == LatexStepType.LATEX_COMPILE }) {
            val viewerIndex = viewerInsertIndex(steps)
            steps.add(viewerIndex, LatexCompileStepOptions())
        }
        ensureViewerStep(steps)

        val viewerIndex = viewerInsertIndex(steps)
        val firstCompileIndex = steps.indexOfFirst { it.type == LatexStepType.LATEX_COMPILE }
        if (firstCompileIndex < 0) {
            return
        }

        val anchorIndex = lastAuxIndexBeforeViewer(steps) ?: firstCompileIndex

        val existingCompilesAfterAnchor = steps.withIndex().count { (index, step) ->
            index in (anchorIndex + 1)..<viewerIndex && step.type == LatexStepType.LATEX_COMPILE
        }
        val requiredCompilesAfterAnchor = requiredCompilesAfterAnchor(steps)
        val missingCompiles = requiredCompilesAfterAnchor - existingCompilesAfterAnchor
        if (missingCompiles <= 0) {
            return
        }

        repeat(missingCompiles) {
            val followUpCompile = steps[firstCompileIndex].deepCopy()
            followUpCompile.id = generateLatexStepId()
            steps.add(viewerInsertIndex(steps), followUpCompile)
        }
    }

    private fun requiredCompilesAfterAnchor(steps: List<LatexStepRunConfigurationOptions>): Int {
        val viewerIndex = viewerInsertIndex(steps)
        val hasBibliographyStep = steps.withIndex().any { (index, step) ->
            index < viewerIndex && step.type == LatexStepType.BIBTEX
        }
        return if (hasBibliographyStep) 2 else 1
    }

    private fun lastAuxIndexBeforeViewer(steps: List<LatexStepRunConfigurationOptions>): Int? {
        val viewerIndex = viewerInsertIndex(steps)
        return steps.withIndex()
            .lastOrNull { (index, step) ->
                index < viewerIndex && step.type in followUpCompileAnchorTypes
            }
            ?.index
    }

    private fun viewerInsertIndex(steps: List<LatexStepRunConfigurationOptions>): Int {
        val viewerIndex = steps.indexOfFirst { it.type == LatexStepType.PDF_VIEWER }
        return if (viewerIndex < 0) steps.size else viewerIndex
    }

    private fun inferRequiredAuxiliarySteps(
        mainPsiFile: PsiFile,
        steps: List<LatexStepRunConfigurationOptions>,
    ): List<LatexStepRunConfigurationOptions> {
        val inferred = mutableListOf<LatexStepRunConfigurationOptions>()
        val signals = collectPsiSignals(mainPsiFile)

        if (shouldAddBibliographyStep(steps, signals)) {
            inferred += BibtexStepOptions()
        }
        if (shouldAddPythontexStep(steps, signals.usedPackages)) {
            inferred += PythontexStepOptions()
        }
        if (shouldAddMakeglossariesStep(steps, signals.usedPackages)) {
            inferred += MakeglossariesStepOptions()
        }
        // NOTE: Xindy auto-detection is temporarily disabled.
        // Keep xindy as an explicit, user-added step only.

        return inferred
    }

    private fun shouldAddBibliographyStep(
        steps: List<LatexStepRunConfigurationOptions>,
        signals: PsiSignals,
    ): Boolean {
        if (!canInferAux(steps, disableForLatexmk = true)) {
            return false
        }
        if (steps.any { it.type == LatexStepType.BIBTEX }) {
            return false
        }
        if (LatexLib.CITATION_STYLE_LANGUAGE in signals.usedPackages) {
            return false
        }

        return signals.hasBibliography || signals.usesBiber || signals.hasAddBibResource
    }

    private fun shouldAddPythontexStep(
        steps: List<LatexStepRunConfigurationOptions>,
        usedPackages: Set<LatexLib>,
    ): Boolean {
        if (!canInferAux(steps)) {
            return false
        }
        if (steps.any { it.type == LatexStepType.PYTHONTEX || it.type == LatexStepType.EXTERNAL_TOOL }) {
            return false
        }
        return LatexLib.PYTHONTEX in usedPackages
    }

    private fun shouldAddMakeglossariesStep(
        steps: List<LatexStepRunConfigurationOptions>,
        usedPackages: Set<LatexLib>,
    ): Boolean {
        if (!canInferAux(steps, disableForLatexmk = true)) {
            return false
        }
        if (steps.any { it.type == LatexStepType.MAKEINDEX || it.type == LatexStepType.MAKEGLOSSARIES }) {
            return false
        }

        return LatexLib.GLOSSARIES in usedPackages || LatexLib.GLOSSARIESEXTRA in usedPackages
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

    private fun canInferAux(
        steps: List<LatexStepRunConfigurationOptions>,
        disableForLatexmk: Boolean = false,
    ): Boolean {
        if (!disableForLatexmk) {
            return true
        }
        return steps.none { it.type == LatexStepType.LATEXMK_COMPILE }
    }

    private fun collectPsiSignals(mainPsiFile: PsiFile): PsiSignals = ReadAction.compute<PsiSignals, RuntimeException> {
        val usedPackages = mainPsiFile.includedPackagesInFileset()

        PsiSignals(
            usedPackages = usedPackages,
            hasBibliography = mainPsiFile.hasBibliography(),
            usesBiber = mainPsiFile.usesBiber(),
            hasAddBibResource = NewCommandsIndex.getByNameInFileSet(CommandNames.ADD_BIB_RESOURCE, mainPsiFile).isNotEmpty(),
        )
    }

    private fun resolveCommandSpec(
        runConfig: LatexRunConfiguration,
        contextPsiFile: PsiFile,
    ): CommandSpec {
        val command = ReadAction.compute<String?, RuntimeException> {
            val magicComments = contextPsiFile.allParentMagicComments()
            val runCommand = magicComments.value(DefaultMagicKeys.COMPILER)
            val runProgram = magicComments.value(DefaultMagicKeys.PROGRAM)
            val cslFallback = if (contextPsiFile.includedPackagesInFileset().contains(LatexLib.CITATION_STYLE_LANGUAGE)) {
                LatexCompiler.LUALATEX.executableName
            }
            else {
                null
            }
            runCommand ?: runProgram ?: cslFallback
        }

        val compilerExecutable = if (command?.contains(' ') == true) {
            command.substring(0, command.indexOf(' ')).trim()
        }
        else {
            command
        }

        val useLatexmk = when {
            LatexCompilePrograms.isLatexmkExecutable(compilerExecutable) -> true
            compilerExecutable == null && runConfig.primaryCompileStep() is LatexmkCompileStepOptions -> true
            else -> false
        }
        val selectedCompiler = if (useLatexmk) {
            null
        }
        else {
            compilerExecutable
                ?.let { LatexCompilePrograms.classicByExecutableName(it) }
                ?: runConfig.primaryCompiler()
                ?: LatexCompiler.PDFLATEX
        }

        val commandArguments = if (command != null) {
            command.removePrefix(compilerExecutable ?: "").trim().ifBlank { null }
        }
        else {
            null
        }

        return CommandSpec(selectedCompiler, commandArguments, useLatexmk)
    }

    private fun resolveWorkingDirectory(
        compiler: LatexCompiler?,
        mainVirtualFile: VirtualFile,
    ): Path? {
        if (compiler != LatexCompiler.TECTONIC || !mainVirtualFile.hasTectonicTomlFile()) {
            return null
        }
        val tectonicToml = mainVirtualFile.findTectonicTomlFile() ?: return null
        return Path.of(tectonicToml.parent.path)
    }

    private fun ensurePrimaryCompileStepClassic(
        steps: MutableList<LatexStepRunConfigurationOptions>
    ): LatexCompileStepOptions {
        val index = steps.indexOfFirst {
            it.enabled && (it is LatexCompileStepOptions || it is LatexmkCompileStepOptions)
        }
        return when {
            index < 0 -> LatexCompileStepOptions().also { steps.add(0, it) }
            steps[index] is LatexCompileStepOptions -> steps[index] as LatexCompileStepOptions
            else -> {
                val old = steps[index] as LatexmkCompileStepOptions
                LatexCompileStepOptions().also {
                    it.id = old.id
                    it.enabled = old.enabled
                    it.compiler = LatexCompiler.PDFLATEX
                    it.compilerPath = old.compilerPath
                    it.compilerArguments = old.compilerArguments
                    it.outputFormat = LatexCompiler.Format.PDF
                    it.beforeRunCommand = old.beforeRunCommand
                    it.selectedOptions = old.selectedOptions
                    steps[index] = it
                }
            }
        }
    }

    private fun ensurePrimaryCompileStepLatexmk(
        steps: MutableList<LatexStepRunConfigurationOptions>
    ): LatexmkCompileStepOptions {
        val index = steps.indexOfFirst {
            it.enabled && (it is LatexCompileStepOptions || it is LatexmkCompileStepOptions)
        }
        return when {
            index < 0 -> LatexmkCompileStepOptions().also { steps.add(0, it) }
            steps[index] is LatexmkCompileStepOptions -> steps[index] as LatexmkCompileStepOptions
            else -> {
                val old = steps[index] as LatexCompileStepOptions
                LatexmkCompileStepOptions().also {
                    it.id = old.id
                    it.enabled = old.enabled
                    it.compilerPath = old.compilerPath
                    it.compilerArguments = old.compilerArguments
                    it.latexmkCompileMode = LatexmkCompileMode.AUTO
                    it.beforeRunCommand = old.beforeRunCommand
                    it.selectedOptions = old.selectedOptions
                    steps[index] = it
                }
            }
        }
    }

    private data class CommandSpec(
        val compiler: LatexCompiler?,
        val arguments: String?,
        val isLatexmk: Boolean,
    )

    private data class PsiSignals(
        val usedPackages: Set<LatexLib>,
        val hasBibliography: Boolean,
        val usesBiber: Boolean,
        val hasAddBibResource: Boolean,
    )
}
