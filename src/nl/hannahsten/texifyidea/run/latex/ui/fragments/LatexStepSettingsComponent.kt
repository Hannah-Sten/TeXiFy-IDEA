package nl.hannahsten.texifyidea.run.latex.ui.fragments

import com.intellij.openapi.Disposable
import com.intellij.openapi.options.SettingsEditor
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Disposer
import nl.hannahsten.texifyidea.run.latex.*
import nl.hannahsten.texifyidea.run.latex.step.LatexRunStepProviders
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
    }

    var changeListener: () -> Unit = {}

    private var selectedStepId: String? = null
    private var selectedStepIndex: Int = -1
    private var selectedStepType: String? = null
    private var selectedStep: LatexStepRunConfigurationOptions? = null
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

    init {
        Disposer.register(editor, this)
        Disposer.register(editor, compileSettings)
        Disposer.register(editor, latexmkSettings)
        Disposer.register(editor, viewerSettings)
        Disposer.register(editor, bibtexSettings)
        Disposer.register(editor, makeindexSettings)
        Disposer.register(editor, externalToolSettings)
        Disposer.register(editor, pythontexSettings)
        Disposer.register(editor, makeglossariesSettings)
        Disposer.register(editor, xindySettings)
        Disposer.register(editor, fileCleanupSettings)
        compileSettings.addSettingsEditorListener { changeListener() }
        latexmkSettings.addSettingsEditorListener { changeListener() }
        viewerSettings.addSettingsEditorListener { changeListener() }
        bibtexSettings.addSettingsEditorListener { changeListener() }
        makeindexSettings.addSettingsEditorListener { changeListener() }
        externalToolSettings.addSettingsEditorListener { changeListener() }
        pythontexSettings.addSettingsEditorListener { changeListener() }
        makeglossariesSettings.addSettingsEditorListener { changeListener() }
        xindySettings.addSettingsEditorListener { changeListener() }
        fileCleanupSettings.addSettingsEditorListener { changeListener() }

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

        showCard(CARD_UNSUPPORTED)
    }

    fun resetEditorFrom() {
        syncSelectionWithCurrentSteps(realignSelectedIndex = true)
        showCardForStepType(selectedStepType)
        resetCurrentCard()
    }

    fun applyEditorTo() {
        flushCurrentCard()
    }

    fun flushCurrentStep() {
        flushCurrentCard()
    }

    fun onStepSelectionChanged(index: Int, stepId: String?, type: String?) {
        flushCurrentCard()

        selectedStepIndex = index
        selectedStepId = stepId
        selectedStepType = canonicalType(type)
        selectedStep = selectedStepId?.let { stepsById[it] }
        showCardForStepType(selectedStepType)
        resetCurrentCard()
    }

    fun onStepsChanged() {
        syncSelectionWithCurrentSteps(realignSelectedIndex = false)
        showCardForStepType(selectedStepType)
        resetCurrentCard()
    }

    internal fun currentCardId(): String = currentCardId

    override fun dispose() {
    }

    private fun showCardForStepType(type: String?) {
        when (type) {
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
                    type.isNullOrBlank() -> "Select a step in Compile sequence to configure it."
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

    private fun syncSelectionWithCurrentSteps(realignSelectedIndex: Boolean) {
        stepsById = shadowSteps.associateBy { it.id }
        if (selectedStepId !in stepsById.keys) {
            selectedStepId = null
            selectedStepIndex = -1
            selectedStepType = null
        }

        selectedStep = selectedStepId?.let { stepsById[it] }
        selectedStepType = selectedStep?.type?.let(::canonicalType) ?: selectedStepType
        if (
            realignSelectedIndex &&
            selectedStep != null &&
            selectedStepIndex !in shadowSteps.indices
        ) {
            selectedStepIndex = shadowSteps.indexOfFirst { it.id == selectedStepId }
        }
    }

    private fun <T : LatexStepRunConfigurationOptions> wrapEditor(editor: SettingsEditor<T>): JPanel = JPanel(BorderLayout()).apply {
        add(editor.component, BorderLayout.CENTER)
    }

    private fun resetCurrentCard(targetStep: LatexStepRunConfigurationOptions? = selectedStep) {
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

    private fun flushCurrentCard() {
        val targetStep = findTargetStep()
        if (targetStep == null) {
            selectedStep = null
            return
        }
        when (currentCardId) {
            CARD_COMPILE -> applyEditorToSelected(compileSettings, targetStep as? LatexCompileStepOptions)
            CARD_LATEXMK -> applyEditorToSelected(latexmkSettings, targetStep as? LatexmkCompileStepOptions)
            CARD_VIEWER -> applyEditorToSelected(viewerSettings, targetStep as? PdfViewerStepOptions)
            CARD_BIBTEX -> applyEditorToSelected(bibtexSettings, targetStep as? BibtexStepOptions)
            CARD_MAKEINDEX -> applyEditorToSelected(makeindexSettings, targetStep as? MakeindexStepOptions)
            CARD_EXTERNAL_TOOL -> applyEditorToSelected(externalToolSettings, targetStep as? ExternalToolStepOptions)
            CARD_PYTHONTEX -> applyEditorToSelected(pythontexSettings, targetStep as? PythontexStepOptions)
            CARD_MAKEGLOSSARIES -> applyEditorToSelected(makeglossariesSettings, targetStep as? MakeglossariesStepOptions)
            CARD_XINDY -> applyEditorToSelected(xindySettings, targetStep as? XindyStepOptions)
            CARD_FILE_CLEANUP -> applyEditorToSelected(fileCleanupSettings, targetStep as? FileCleanupStepOptions)
        }
        selectedStep = targetStep
    }

    private fun findTargetStep(): LatexStepRunConfigurationOptions? {
        selectedStepId?.let { id ->
            shadowSteps.firstOrNull { it.id == id }?.let { return it }
        }
        if (selectedStepIndex in shadowSteps.indices) {
            val byIndex = shadowSteps[selectedStepIndex]
            val expectedType = selectedStepType
            if (expectedType == null || canonicalType(byIndex.type) == expectedType) {
                return byIndex
            }
        }
        val expectedType = selectedStepType ?: return null
        return shadowSteps.firstOrNull { canonicalType(it.type) == expectedType }
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
