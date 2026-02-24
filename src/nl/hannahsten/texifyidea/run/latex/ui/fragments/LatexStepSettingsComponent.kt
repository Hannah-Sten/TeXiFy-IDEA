package nl.hannahsten.texifyidea.run.latex.ui.fragments

import com.intellij.execution.ui.FragmentedSettings
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
        private const val CARD_UNSUPPORTED = "unsupported"
    }

    var changeListener: () -> Unit = {}

    private var selectedStepId: String? = null
    private var selectedStepType: String? = null
    private var stepsById: Map<String, LatexStepRunConfigurationOptions> = emptyMap()
    private var boundRunConfig: LatexRunConfiguration? = null

    private var currentCardId: String = CARD_UNSUPPORTED

    private val cardLayout = CardLayout()
    private val cardsPanel = JPanel(cardLayout)

    private val compileState = StepFragmentedState()
    private val latexmkState = StepFragmentedState()
    private val viewerState = StepFragmentedState()

    private val compileSettings = LatexCompileStepFragmentedEditor(project, compileState)
    private val latexmkSettings = LatexmkStepFragmentedEditor(project, latexmkState)
    private val viewerSettings = LatexViewerStepFragmentedEditor(viewerState)
    private val unsupportedSettings = LatexUnsupportedStepSettingsComponent()

    init {
        Disposer.register(parentDisposable, this)
        Disposer.register(parentDisposable, compileSettings)
        Disposer.register(parentDisposable, latexmkSettings)
        Disposer.register(parentDisposable, viewerSettings)
        compileSettings.addSettingsEditorListener { changeListener() }
        latexmkSettings.addSettingsEditorListener { changeListener() }
        viewerSettings.addSettingsEditorListener { changeListener() }

        cardsPanel.add(wrapEditor(compileSettings), CARD_COMPILE)
        cardsPanel.add(wrapEditor(latexmkSettings), CARD_LATEXMK)
        cardsPanel.add(wrapEditor(viewerSettings), CARD_VIEWER)
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
            selectedStepType = null
        }

        bindSelectedStepToState()
        showCardForStepType(selectedStepType)
    }

    fun applyEditorTo(runConfig: LatexRunConfiguration) {
        applyStateIfBound(runConfig, compileState, compileSettings)
        applyStateIfBound(runConfig, latexmkState, latexmkSettings)
        applyStateIfBound(runConfig, viewerState, viewerSettings)
    }

    fun onStepSelectionChanged(index: Int, stepId: String?, type: String?) {
        val runConfig = boundRunConfig ?: return
        flushCurrentCard(runConfig)

        selectedStepId = stepId
        selectedStepType = canonicalType(type)
        bindSelectedStepToState()
        showCardForStepType(selectedStepType)
    }

    fun onStepsChanged(steps: List<LatexStepRunConfigurationOptions>) {
        val runConfig = boundRunConfig ?: return
        flushCurrentCard(runConfig)

        stepsById = steps.associateBy { it.id }
        if (selectedStepId !in stepsById.keys) {
            selectedStepId = null
            selectedStepType = null
        }

        bindSelectedStepToState()
        showCardForStepType(selectedStepType)
    }

    internal fun currentCardId(): String = currentCardId

    override fun dispose() {
    }

    private fun showCardForStepType(type: String?) {
        when (type) {
            LatexStepType.LATEX_COMPILE -> showCard(CARD_COMPILE)
            LatexStepType.LATEXMK_COMPILE -> showCard(CARD_LATEXMK)
            LatexStepType.PDF_VIEWER -> showCard(CARD_VIEWER)
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

    private fun wrapEditor(editor: SettingsEditor<StepFragmentedState>): JPanel = JPanel(BorderLayout()).apply {
        add(editor.component, BorderLayout.CENTER)
    }

    private fun bindSelectedStepToState() {
        boundRunConfig ?: return
        val selectedStep = selectedStepId?.let { stepsById[it] }

        when (selectedStep) {
            is LatexCompileStepOptions -> {
                bindStateForStep(compileState, selectedStep)
                compileSettings.resetFrom(compileState)
            }

            is LatexmkCompileStepOptions -> {
                bindStateForStep(latexmkState, selectedStep)
                latexmkSettings.resetFrom(latexmkState)
            }

            is PdfViewerStepOptions -> {
                bindStateForStep(viewerState, selectedStep)
                viewerSettings.resetFrom(viewerState)
            }

            else -> {
            }
        }
    }

    private fun bindStateForStep(
        state: StepFragmentedState,
        step: LatexStepRunConfigurationOptions,
    ) {
        state.selectedStepOptions = step
        state.selectedOptions = step.selectedOptions
            .map { FragmentedSettings.Option(it.name ?: "", it.visible) }
            .toMutableList()
    }

    private fun flushCurrentCard(runConfig: LatexRunConfiguration) {
        when (currentCardId) {
            CARD_COMPILE -> {
                compileSettings.applyTo(compileState)
                persistStepOptions(runConfig, compileState)
            }

            CARD_LATEXMK -> {
                latexmkSettings.applyTo(latexmkState)
                persistStepOptions(runConfig, latexmkState)
            }

            CARD_VIEWER -> {
                viewerSettings.applyTo(viewerState)
                persistStepOptions(runConfig, viewerState)
            }
        }
    }

    private fun persistStepOptions(
        runConfig: LatexRunConfiguration,
        state: StepFragmentedState,
    ) {
        val stepId = state.selectedStepOptions?.id ?: return
        val targetStep = runConfig.configOptions.steps.firstOrNull { it.id == stepId } ?: state.selectedStepOptions ?: return
        targetStep.selectedOptions = state.selectedOptions
            .filter { it.visible }
            .mapNotNull { option ->
                option.name?.trim()?.takeIf(String::isNotBlank)?.let { FragmentedSettings.Option(it, true) }
            }
            .toMutableList()
        state.selectedStepOptions = targetStep
    }

    private fun applyStateIfBound(
        runConfig: LatexRunConfiguration,
        state: StepFragmentedState,
        editor: SettingsEditor<StepFragmentedState>,
    ) {
        val selected = state.selectedStepOptions ?: return
        val targetStep = runConfig.configOptions.steps.firstOrNull { it.id == selected.id }
            ?: runConfig.configOptions.steps.firstOrNull { it.type == selected.type }
            ?: defaultStepFor(selected.type)?.also { runConfig.configOptions.steps.add(it) }
            ?: return

        state.selectedStepOptions = targetStep
        editor.applyTo(state)
        persistStepOptions(runConfig, state)
    }
}
