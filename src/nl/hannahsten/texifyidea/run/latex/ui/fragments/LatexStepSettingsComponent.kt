package nl.hannahsten.texifyidea.run.latex.ui.fragments

import com.intellij.openapi.Disposable
import com.intellij.openapi.options.SettingsEditor
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Disposer
import nl.hannahsten.texifyidea.run.latex.*
import nl.hannahsten.texifyidea.run.latex.step.LatexRunStepProviders
import java.awt.BorderLayout
import java.awt.CardLayout
import javax.swing.JPanel

internal class LatexStepSettingsComponent(
    parentDisposable: Disposable,
    project: Project,
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
        private const val CARD_UNSUPPORTED = "unsupported"
    }

    var changeListener: () -> Unit = {}

    private var selectedStepId: String? = null
    private var selectedStepIndex: Int = -1
    private var selectedStepType: String? = null
    private var selectedStep: LatexStepRunConfigurationOptions? = null
    private var stepsById: Map<String, LatexStepRunConfigurationOptions> = emptyMap()
    private var boundRunConfig: LatexRunConfiguration? = null

    private var currentCardId: String = CARD_UNSUPPORTED

    private val cardLayout = CardLayout()
    private val cardsPanel = JPanel(cardLayout)

    private val compileSettings = LatexCompileStepFragmentedEditor(project)
    private val latexmkSettings = LatexmkStepFragmentedEditor(project)
    private val viewerSettings = LatexViewerStepFragmentedEditor()
    private val bibtexSettings = BibtexStepFragmentedEditor(project)
    private val makeindexSettings = MakeindexStepFragmentedEditor(project)
    private val externalToolSettings = ExternalToolStepFragmentedEditor(project)
    private val pythontexSettings = PythontexStepFragmentedEditor(project)
    private val makeglossariesSettings = MakeglossariesStepFragmentedEditor(project)
    private val xindySettings = XindyStepFragmentedEditor(project)
    private val unsupportedSettings = LatexUnsupportedStepSettingsComponent()

    init {
        Disposer.register(parentDisposable, this)
        Disposer.register(parentDisposable, compileSettings)
        Disposer.register(parentDisposable, latexmkSettings)
        Disposer.register(parentDisposable, viewerSettings)
        Disposer.register(parentDisposable, bibtexSettings)
        Disposer.register(parentDisposable, makeindexSettings)
        Disposer.register(parentDisposable, externalToolSettings)
        Disposer.register(parentDisposable, pythontexSettings)
        Disposer.register(parentDisposable, makeglossariesSettings)
        Disposer.register(parentDisposable, xindySettings)
        compileSettings.addSettingsEditorListener { changeListener() }
        latexmkSettings.addSettingsEditorListener { changeListener() }
        viewerSettings.addSettingsEditorListener { changeListener() }
        bibtexSettings.addSettingsEditorListener { changeListener() }
        makeindexSettings.addSettingsEditorListener { changeListener() }
        externalToolSettings.addSettingsEditorListener { changeListener() }
        pythontexSettings.addSettingsEditorListener { changeListener() }
        makeglossariesSettings.addSettingsEditorListener { changeListener() }
        xindySettings.addSettingsEditorListener { changeListener() }

        cardsPanel.add(wrapEditor(compileSettings), CARD_COMPILE)
        cardsPanel.add(wrapEditor(latexmkSettings), CARD_LATEXMK)
        cardsPanel.add(wrapEditor(viewerSettings), CARD_VIEWER)
        cardsPanel.add(wrapEditor(bibtexSettings), CARD_BIBTEX)
        cardsPanel.add(wrapEditor(makeindexSettings), CARD_MAKEINDEX)
        cardsPanel.add(wrapEditor(externalToolSettings), CARD_EXTERNAL_TOOL)
        cardsPanel.add(wrapEditor(pythontexSettings), CARD_PYTHONTEX)
        cardsPanel.add(wrapEditor(makeglossariesSettings), CARD_MAKEGLOSSARIES)
        cardsPanel.add(wrapEditor(xindySettings), CARD_XINDY)
        cardsPanel.add(unsupportedSettings, CARD_UNSUPPORTED)
        add(cardsPanel, BorderLayout.CENTER)

        showCard(CARD_UNSUPPORTED)
    }

    fun resetEditorFrom(runConfig: LatexRunConfiguration) {
        boundRunConfig = runConfig
        runConfig.configOptions.ensureDefaultSteps()
        stepsById = runConfig.configOptions.steps.associateBy { it.id }

        if (selectedStepId !in stepsById.keys) {
            selectedStepId = null
            selectedStepIndex = -1
            selectedStepType = null
        }

        selectedStep = selectedStepId?.let { stepsById[it] }
        if (selectedStepType == null) {
            selectedStepType = selectedStep?.type?.let(::canonicalType)
        }
        if (selectedStep != null && selectedStepIndex !in runConfig.configOptions.steps.indices) {
            selectedStepIndex = runConfig.configOptions.steps.indexOfFirst { it.id == selectedStepId }
        }
        showCardForStepType(selectedStepType)
        resetCurrentCard()
    }

    fun applyEditorTo(runConfig: LatexRunConfiguration) {
        flushCurrentCard(runConfig)
    }

    fun onStepSelectionChanged(index: Int, stepId: String?, type: String?) {
        val runConfig = boundRunConfig ?: return
        flushCurrentCard(runConfig)

        selectedStepIndex = index
        selectedStepId = stepId
        selectedStepType = canonicalType(type)
        selectedStep = selectedStepId?.let { stepsById[it] }
        showCardForStepType(selectedStepType)
        resetCurrentCard()
    }

    fun onStepsChanged(steps: List<LatexStepRunConfigurationOptions>) {
        val runConfig = boundRunConfig ?: return
        flushCurrentCard(runConfig)

        stepsById = steps.associateBy { it.id }
        if (selectedStepId !in stepsById.keys) {
            selectedStepId = null
            selectedStepIndex = -1
            selectedStepType = null
        }

        selectedStep = selectedStepId?.let { stepsById[it] }
        if (selectedStepType == null) {
            selectedStepType = selectedStep?.type?.let(::canonicalType)
        }
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

    private fun <T : LatexStepRunConfigurationOptions> wrapEditor(editor: SettingsEditor<T>): JPanel = JPanel(BorderLayout()).apply {
        add(editor.component, BorderLayout.CENTER)
    }

    private fun resetCurrentCard() {
        when (currentCardId) {
            CARD_COMPILE -> resetEditorFromSelected(compileSettings, selectedStep as? LatexCompileStepOptions)
            CARD_LATEXMK -> resetEditorFromSelected(latexmkSettings, selectedStep as? LatexmkCompileStepOptions)
            CARD_VIEWER -> resetEditorFromSelected(viewerSettings, selectedStep as? PdfViewerStepOptions)
            CARD_BIBTEX -> resetEditorFromSelected(bibtexSettings, selectedStep as? BibtexStepOptions)
            CARD_MAKEINDEX -> resetEditorFromSelected(makeindexSettings, selectedStep as? MakeindexStepOptions)
            CARD_EXTERNAL_TOOL -> resetEditorFromSelected(externalToolSettings, selectedStep as? ExternalToolStepOptions)
            CARD_PYTHONTEX -> resetEditorFromSelected(pythontexSettings, selectedStep as? PythontexStepOptions)
            CARD_MAKEGLOSSARIES -> resetEditorFromSelected(makeglossariesSettings, selectedStep as? MakeglossariesStepOptions)
            CARD_XINDY -> resetEditorFromSelected(xindySettings, selectedStep as? XindyStepOptions)
        }
    }

    private fun flushCurrentCard(runConfig: LatexRunConfiguration) {
        val targetStep = findTargetStep(runConfig)
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
        }
        selectedStep = targetStep
    }

    private fun findTargetStep(runConfig: LatexRunConfiguration): LatexStepRunConfigurationOptions? {
        val steps = runConfig.configOptions.steps
        selectedStepId?.let { id ->
            steps.firstOrNull { it.id == id }?.let { return it }
        }
        if (selectedStepIndex in steps.indices) {
            val byIndex = steps[selectedStepIndex]
            val expectedType = selectedStepType
            if (expectedType == null || canonicalType(byIndex.type) == expectedType) {
                return byIndex
            }
        }
        val expectedType = selectedStepType ?: return null
        return steps.firstOrNull { canonicalType(it.type) == expectedType }
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
