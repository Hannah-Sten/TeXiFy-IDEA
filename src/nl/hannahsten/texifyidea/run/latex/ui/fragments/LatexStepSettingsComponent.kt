package nl.hannahsten.texifyidea.run.latex.ui.fragments

import com.intellij.execution.ui.FragmentedSettings
import com.intellij.openapi.Disposable
import com.intellij.openapi.options.SettingsEditor
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Disposer
import nl.hannahsten.texifyidea.run.compiler.LatexCompiler
import nl.hannahsten.texifyidea.run.latex.*
import nl.hannahsten.texifyidea.run.latex.step.BibtexRunStep
import nl.hannahsten.texifyidea.run.latex.step.LatexRunStepProviders
import nl.hannahsten.texifyidea.run.latex.step.MakeindexRunStep
import nl.hannahsten.texifyidea.run.latex.ui.LatexSettingsEditor
import java.awt.BorderLayout
import java.awt.CardLayout
import javax.swing.JPanel

internal class LatexStepSettingsComponent(
    project: Project,
    private val editor: LatexSettingsEditor,
) : JPanel(BorderLayout()), Disposable {

    private companion object {

        private const val CARD_COMPILE = "compile"
        private const val CARD_LATEXMK = "latexmk"
        private const val CARD_VIEWER = "viewer"
        private const val CARD_BIBTEX = "bibtex"
        private const val CARD_MAKEINDEX = "makeindex"
        private const val CARD_EXTERNAL_TOOL = "externalTool"
        private const val CARD_PYTHONTEX = "pythontex"
        private const val CARD_MAKEGLOSSARIES = "makeglossaries"
        private const val CARD_XINDY = "xindy"
        private const val CARD_FILE_CLEANUP = "fileCleanup"
        private const val CARD_UNSUPPORTED = "unsupported"

        private const val NO_SELECTION_MESSAGE = "Select a step in Compile sequence to configure it."
        private const val MIXED_SELECTION_MESSAGE =
            "Batch editing is available only when all selected steps have the same type."
    }

    private enum class StepSelectionMode {
        NONE,
        SINGLE,
        MULTI_SAME_TYPE,
        MULTI_MIXED_TYPE,
    }

    var changeListener: () -> Unit = {}

    private var selectionState: LatexStepSelectionState = LatexStepSelectionState.EMPTY
    private var stepsById: Map<String, LatexStepRunConfigurationOptions> = emptyMap()

    private var currentCardId: String = CARD_UNSUPPORTED

    private val cardLayout = CardLayout()
    private val cardsPanel = JPanel(cardLayout)
    private val shadowSteps: MutableList<LatexStepRunConfigurationOptions>
        get() = editor.shadowSteps

    private val compileSettings = LatexCompileStepFragmentedEditor(project)
    private val latexmkSettings = LatexmkStepFragmentedEditor()
    private val viewerSettings = LatexViewerStepFragmentedEditor()
    private val bibtexSettings = BibtexStepFragmentedEditor(project)
    private val makeindexSettings = MakeindexStepFragmentedEditor(project)
    private val externalToolSettings = ExternalToolStepFragmentedEditor(project)
    private val pythontexSettings = PythontexStepFragmentedEditor(project)
    private val makeglossariesSettings = MakeglossariesStepFragmentedEditor(project)
    private val xindySettings = XindyStepFragmentedEditor(project)
    private val fileCleanupSettings = FileCleanupStepFragmentedEditor()
    private val unsupportedSettings = LatexUnsupportedStepSettingsComponent()
    private val settingsEditors = listOf(
        compileSettings,
        latexmkSettings,
        viewerSettings,
        bibtexSettings,
        makeindexSettings,
        externalToolSettings,
        pythontexSettings,
        makeglossariesSettings,
        xindySettings,
        fileCleanupSettings,
    )

    init {
        Disposer.register(editor, this)
        settingsEditors.forEach {
            Disposer.register(editor, it)
            it.addSettingsEditorListener { changeListener() }
        }

        cardsPanel.add(wrapEditor(compileSettings), CARD_COMPILE)
        cardsPanel.add(wrapEditor(latexmkSettings), CARD_LATEXMK)
        cardsPanel.add(wrapEditor(viewerSettings), CARD_VIEWER)
        cardsPanel.add(wrapEditor(bibtexSettings), CARD_BIBTEX)
        cardsPanel.add(wrapEditor(makeindexSettings), CARD_MAKEINDEX)
        cardsPanel.add(wrapEditor(externalToolSettings), CARD_EXTERNAL_TOOL)
        cardsPanel.add(wrapEditor(pythontexSettings), CARD_PYTHONTEX)
        cardsPanel.add(wrapEditor(makeglossariesSettings), CARD_MAKEGLOSSARIES)
        cardsPanel.add(wrapEditor(xindySettings), CARD_XINDY)
        cardsPanel.add(wrapEditor(fileCleanupSettings), CARD_FILE_CLEANUP)
        cardsPanel.add(unsupportedSettings, CARD_UNSUPPORTED)

        add(cardsPanel, BorderLayout.CENTER)

        refreshForCurrentSelection()
    }

    fun resetEditorFrom() {
        syncSelectionWithCurrentSteps()
        refreshForCurrentSelection()
    }

    fun applyEditorTo() {
        flushCurrentCard()
    }

    fun flushCurrentStep() {
        flushCurrentCard()
    }

    fun onStepSelectionChanged(selectionState: LatexStepSelectionState) {
        flushCurrentCard(selectionChangeFlushTargets(selectionState))

        this.selectionState = selectionState
        syncSelectionWithCurrentSteps()
        refreshForCurrentSelection()
    }

    fun onStepsChanged() {
        syncSelectionWithCurrentSteps()
        refreshForCurrentSelection()
    }

    internal fun currentCardId(): String = currentCardId

    internal fun currentUnsupportedMessageForTest(): String = unsupportedSettings.message()

    internal fun currentBibtexWorkingDirectoryHintForTest(): String? = bibtexSettings.inferredWorkingDirectoryHintForTest()

    internal fun currentMakeindexWorkingDirectoryHintForTest(): String? = makeindexSettings.inferredWorkingDirectoryHintForTest()

    internal fun setCompileEditorValuesForTest(
        compilerPath: String,
        compilerArguments: String,
    ) {
        compileSettings.setValuesForTest(compilerPath, compilerArguments)
    }

    internal fun setCompileCompilerForTest(compiler: LatexCompiler) {
        compileSettings.setCompilerForTest(compiler)
    }

    internal fun setViewerEditorValuesForTest(
        pdfViewerName: String,
        customViewerCommand: String?,
    ) {
        viewerSettings.setValuesForTest(pdfViewerName, customViewerCommand)
    }

    override fun dispose() {
    }

    private fun refreshForCurrentSelection() {
        val selectionMode = selectionMode()
        when (selectionMode) {
            StepSelectionMode.NONE -> {
                unsupportedSettings.setMessage(NO_SELECTION_MESSAGE)
                showCard(CARD_UNSUPPORTED)
            }

            StepSelectionMode.MULTI_MIXED_TYPE -> {
                unsupportedSettings.setMessage(MIXED_SELECTION_MESSAGE)
                showCard(CARD_UNSUPPORTED)
            }

            StepSelectionMode.SINGLE,
            StepSelectionMode.MULTI_SAME_TYPE,
            -> {
                val primaryStep = primarySelectedStep()
                showCardForStepType(primaryStep?.type)
                resetCurrentCard(templateStepForReset(selectionMode))
            }
        }
    }

    private fun showCardForStepType(type: String?) {
        when (canonicalType(type)) {
            LatexStepType.LATEX_COMPILE -> showCard(CARD_COMPILE)
            LatexStepType.LATEXMK_COMPILE -> showCard(CARD_LATEXMK)
            LatexStepType.PDF_VIEWER -> showCard(CARD_VIEWER)
            LatexStepType.BIBTEX -> showCard(CARD_BIBTEX)
            LatexStepType.MAKEINDEX -> showCard(CARD_MAKEINDEX)
            LatexStepType.EXTERNAL_TOOL -> showCard(CARD_EXTERNAL_TOOL)
            LatexStepType.PYTHONTEX -> showCard(CARD_PYTHONTEX)
            LatexStepType.MAKEGLOSSARIES -> showCard(CARD_MAKEGLOSSARIES)
            LatexStepType.XINDY -> showCard(CARD_XINDY)
            LatexStepType.FILE_CLEANUP -> showCard(CARD_FILE_CLEANUP)
            else -> {
                val message = when {
                    type.isNullOrBlank() -> NO_SELECTION_MESSAGE
                    else -> "${LatexStepUiSupport.description(type)} settings are not available yet."
                }
                unsupportedSettings.setMessage(message)
                showCard(CARD_UNSUPPORTED)
            }
        }
    }

    private fun showCard(cardId: String) {
        currentCardId = cardId
        cardLayout.show(cardsPanel, cardId)
        revalidate()
        repaint()
    }

    private fun canonicalType(type: String?): String? {
        if (type.isNullOrBlank()) {
            return null
        }
        return LatexRunStepProviders.find(type)?.type ?: type.trim().lowercase()
    }

    private fun syncSelectionWithCurrentSteps() {
        stepsById = shadowSteps.associateBy { it.id }
        val selectedIds = selectionState.selectedStepIds
            .filter { it in stepsById.keys }
        val primaryStepId = selectionState.primaryStepId
            ?.takeIf { it in selectedIds }
            ?: selectedIds.lastOrNull()
        selectionState = LatexStepSelectionState(
            selectedStepIds = selectedIds,
            primaryStepId = primaryStepId,
        )
    }

    private fun selectedSteps(): List<LatexStepRunConfigurationOptions> = selectionState.selectedStepIds
        .mapNotNull { stepsById[it] }

    private fun primarySelectedStep(): LatexStepRunConfigurationOptions? = selectionState.primaryStepId
        ?.let { stepsById[it] }

    private fun selectionMode(): StepSelectionMode {
        val selectedSteps = selectedSteps()
        if (selectedSteps.isEmpty()) {
            return StepSelectionMode.NONE
        }
        if (selectedSteps.size == 1) {
            return StepSelectionMode.SINGLE
        }

        val distinctTypes = selectedSteps
            .mapNotNull { canonicalType(it.type) }
            .distinct()
        return if (distinctTypes.size == 1) {
            StepSelectionMode.MULTI_SAME_TYPE
        }
        else {
            StepSelectionMode.MULTI_MIXED_TYPE
        }
    }

    private fun templateStepForReset(selectionMode: StepSelectionMode): LatexStepRunConfigurationOptions? = when (selectionMode) {
        StepSelectionMode.SINGLE -> primarySelectedStep()
        StepSelectionMode.MULTI_SAME_TYPE -> primarySelectedStep()?.deepCopy()
        else -> null
    }

    private fun <T : LatexStepRunConfigurationOptions> wrapEditor(editor: SettingsEditor<T>): JPanel = JPanel(BorderLayout()).apply {
        add(editor.component, BorderLayout.CENTER)
    }

    private fun resetCurrentCard(targetStep: LatexStepRunConfigurationOptions?) {
        updateCardContext(targetStep)
        when (currentCardId) {
            CARD_COMPILE -> resetEditorFromSelected(compileSettings, targetStep as? LatexCompileStepOptions)
            CARD_LATEXMK -> resetEditorFromSelected(latexmkSettings, targetStep as? LatexmkCompileStepOptions)
            CARD_VIEWER -> resetEditorFromSelected(viewerSettings, targetStep as? PdfViewerStepOptions)
            CARD_BIBTEX -> resetEditorFromSelected(bibtexSettings, targetStep as? BibtexStepOptions)
            CARD_MAKEINDEX -> resetEditorFromSelected(makeindexSettings, targetStep as? MakeindexStepOptions)
            CARD_EXTERNAL_TOOL -> resetEditorFromSelected(externalToolSettings, targetStep as? ExternalToolStepOptions)
            CARD_PYTHONTEX -> resetEditorFromSelected(pythontexSettings, targetStep as? PythontexStepOptions)
            CARD_MAKEGLOSSARIES -> resetEditorFromSelected(makeglossariesSettings, targetStep as? MakeglossariesStepOptions)
            CARD_XINDY -> resetEditorFromSelected(xindySettings, targetStep as? XindyStepOptions)
            CARD_FILE_CLEANUP -> resetEditorFromSelected(fileCleanupSettings, targetStep as? FileCleanupStepOptions)
        }
    }

    private fun updateCardContext(targetStep: LatexStepRunConfigurationOptions?) {
        val runConfig = editor.configForUiContext()
        (targetStep as? BibtexStepOptions)?.let {
            BibtexRunStep.inferredWorkingDirectoryHint(runConfig)
        }?.let {
            bibtexSettings.setInferredWorkingDirectoryHint(it)
        }
        (targetStep as? MakeindexStepOptions)?.let {
            MakeindexRunStep.inferredWorkingDirectoryHint(runConfig, it)
        }?.let {
            makeindexSettings.setInferredWorkingDirectoryHint(it)
        }
    }

    private fun selectionChangeFlushTargets(newSelectionState: LatexStepSelectionState): Set<String> = when (selectionMode()) {
        StepSelectionMode.SINGLE -> selectionState.selectedStepIds.toSet()
        StepSelectionMode.MULTI_SAME_TYPE ->
            selectionState.selectedStepIds
                .intersect(newSelectionState.selectedStepIds.toSet())

        StepSelectionMode.NONE,
        StepSelectionMode.MULTI_MIXED_TYPE,
        -> emptySet()
    }

    private fun flushCurrentCard(targetStepIds: Set<String> = selectionState.selectedStepIds.toSet()) {
        if (
            targetStepIds.isEmpty() ||
            selectionMode() == StepSelectionMode.NONE ||
            selectionMode() == StepSelectionMode.MULTI_MIXED_TYPE ||
            currentCardId == CARD_UNSUPPORTED
        ) {
            return
        }

        val primaryStep = primarySelectedStep() ?: return
        val template = primaryStep.deepCopy()
        when (currentCardId) {
            CARD_COMPILE -> applyEditorToSelected(compileSettings, template as? LatexCompileStepOptions)
            CARD_LATEXMK -> applyEditorToSelected(latexmkSettings, template as? LatexmkCompileStepOptions)
            CARD_VIEWER -> applyEditorToSelected(viewerSettings, template as? PdfViewerStepOptions)
            CARD_BIBTEX -> applyEditorToSelected(bibtexSettings, template as? BibtexStepOptions)
            CARD_MAKEINDEX -> applyEditorToSelected(makeindexSettings, template as? MakeindexStepOptions)
            CARD_EXTERNAL_TOOL -> applyEditorToSelected(externalToolSettings, template as? ExternalToolStepOptions)
            CARD_PYTHONTEX -> applyEditorToSelected(pythontexSettings, template as? PythontexStepOptions)
            CARD_MAKEGLOSSARIES -> applyEditorToSelected(makeglossariesSettings, template as? MakeglossariesStepOptions)
            CARD_XINDY -> applyEditorToSelected(xindySettings, template as? XindyStepOptions)
            CARD_FILE_CLEANUP -> applyEditorToSelected(fileCleanupSettings, template as? FileCleanupStepOptions)
        }
        template.selectedOptions = currentSelectedOptionsForCurrentCard()

        applyTemplateToSelectedSteps(template, targetStepIds)
        syncSelectionWithCurrentSteps()
    }

    private fun currentSelectedOptionsForCurrentCard(): MutableList<FragmentedSettings.Option> = when (currentCardId) {
        CARD_COMPILE -> compileSettings.currentSelectedOptions()
        CARD_LATEXMK -> latexmkSettings.currentSelectedOptions()
        CARD_VIEWER -> viewerSettings.currentSelectedOptions()
        CARD_BIBTEX -> bibtexSettings.currentSelectedOptions()
        CARD_MAKEINDEX -> makeindexSettings.currentSelectedOptions()
        CARD_EXTERNAL_TOOL -> externalToolSettings.currentSelectedOptions()
        CARD_PYTHONTEX -> pythontexSettings.currentSelectedOptions()
        CARD_MAKEGLOSSARIES -> makeglossariesSettings.currentSelectedOptions()
        CARD_XINDY -> xindySettings.currentSelectedOptions()
        CARD_FILE_CLEANUP -> fileCleanupSettings.currentSelectedOptions()
        else -> mutableListOf()
    }

    private fun applyTemplateToSelectedSteps(
        template: LatexStepRunConfigurationOptions,
        selectedIds: Set<String>,
    ) {
        shadowSteps.forEachIndexed { index, target ->
            if (target.id !in selectedIds) {
                return@forEachIndexed
            }

            if (target::class == template::class) {
                applyTemplateInPlace(target, template)
            }
            else {
                shadowSteps[index] = template.copyWithIdentity(target.id)
            }
        }
        stepsById = shadowSteps.associateBy { it.id }
    }

    private fun applyTemplateInPlace(
        target: LatexStepRunConfigurationOptions,
        template: LatexStepRunConfigurationOptions,
    ) {
        val originalId = target.id
        target.copyFrom(template)
        target.id = originalId
        target.selectedOptions = template.selectedOptions
            .map { source ->
                FragmentedSettings.Option().also { option ->
                    option.copyFrom(source)
                }
            }
            .toMutableList()
    }

    private fun <T : LatexStepRunConfigurationOptions> resetEditorFromSelected(
        editor: SettingsEditor<T>,
        step: T?,
    ) {
        if (step == null) {
            return
        }
        editor.resetFrom(step)
    }

    private fun <T : LatexStepRunConfigurationOptions> applyEditorToSelected(
        editor: SettingsEditor<T>,
        step: T?,
    ) {
        if (step == null) {
            return
        }
        editor.applyTo(step)
    }
}
