package nl.hannahsten.texifyidea.run.latex.ui.fragments

import com.intellij.openapi.Disposable
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Disposer
import com.intellij.openapi.options.SettingsEditor
import nl.hannahsten.texifyidea.run.latex.LatexRunConfiguration
import nl.hannahsten.texifyidea.run.latex.StepUiOptionIds
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

    private var selectedStepType: String? = null
    private var stepTypesInSequence: List<String> = emptyList()
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
        resetState(
            state = compileState,
            runConfig = runConfig,
            stepType = StepUiOptionIds.LATEX_COMPILE
        )
        resetState(
            state = latexmkState,
            runConfig = runConfig,
            stepType = StepUiOptionIds.LATEXMK_COMPILE
        )
        resetState(
            state = viewerState,
            runConfig = runConfig,
            stepType = StepUiOptionIds.PDF_VIEWER
        )
        compileSettings.resetFrom(compileState)
        latexmkSettings.resetFrom(latexmkState)
        viewerSettings.resetFrom(viewerState)
        showCardForStepType(selectedStepType)
    }

    fun applyEditorTo(runConfig: LatexRunConfiguration) {
        compileState.runConfig = runConfig
        latexmkState.runConfig = runConfig
        viewerState.runConfig = runConfig

        when (activeCompileStepType()) {
            StepUiOptionIds.LATEXMK_COMPILE -> latexmkSettings.applyTo(latexmkState)
            StepUiOptionIds.LATEX_COMPILE -> compileSettings.applyTo(compileState)
        }
        viewerSettings.applyTo(viewerState)
        runConfig.stepUiOptionIdsByType = mutableMapOf<String, MutableSet<String>>().apply {
            putIfNotEmpty(StepUiOptionIds.LATEX_COMPILE, optionIdsOf(compileState))
            putIfNotEmpty(StepUiOptionIds.LATEXMK_COMPILE, optionIdsOf(latexmkState))
            putIfNotEmpty(StepUiOptionIds.PDF_VIEWER, optionIdsOf(viewerState))
        }
    }

    fun onStepSelectionChanged(index: Int, type: String?) {
        selectedStepType = canonicalType(type)
        showCardForStepType(selectedStepType)
    }

    fun onStepTypesChanged(types: List<String>) {
        stepTypesInSequence = types.mapNotNull(::canonicalType)
    }

    internal fun currentCardId(): String = currentCardId

    override fun dispose() {
    }

    private fun showCardForStepType(type: String?) {
        when (type) {
            StepUiOptionIds.LATEX_COMPILE,
            -> {
                showCard(CARD_COMPILE)
            }

            StepUiOptionIds.LATEXMK_COMPILE -> {
                showCard(CARD_LATEXMK)
            }

            StepUiOptionIds.PDF_VIEWER -> {
                showCard(CARD_VIEWER)
            }

            else -> {
                val message = when {
                    type.isNullOrBlank() -> "Select a step in Compile sequence to configure it."
                    else -> "${LatexStepUiSupport.description(type)} settings are not available yet. Use Advanced options (legacy)."
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

    private fun activeCompileStepType(): String? {
        val firstCompileType = stepTypesInSequence.firstOrNull {
            it == StepUiOptionIds.LATEX_COMPILE || it == StepUiOptionIds.LATEXMK_COMPILE
        }
        return when (firstCompileType) {
            StepUiOptionIds.LATEXMK_COMPILE -> StepUiOptionIds.LATEXMK_COMPILE
            StepUiOptionIds.LATEX_COMPILE -> StepUiOptionIds.LATEX_COMPILE
            else -> selectedStepType
        }
    }

    private fun wrapEditor(editor: SettingsEditor<StepFragmentedState>): JPanel = JPanel(BorderLayout()).apply {
        add(editor.component, BorderLayout.CENTER)
    }

    private fun resetState(
        state: StepFragmentedState,
        runConfig: LatexRunConfiguration,
        stepType: String,
    ) {
        state.runConfig = runConfig
        state.selectedOptions = runConfig.stepUiOptionIdsByType[stepType]
            .orEmpty()
            .map { com.intellij.execution.ui.FragmentedSettings.Option(it, true) }
            .toMutableList()
    }

    private fun optionIdsOf(state: StepFragmentedState): MutableSet<String> = state.selectedOptions
        .asSequence()
        .filter { it.visible }
        .mapNotNull { it.name?.trim()?.takeIf(String::isNotBlank) }
        .toMutableSet()

    private fun MutableMap<String, MutableSet<String>>.putIfNotEmpty(type: String, ids: MutableSet<String>) {
        if (ids.isNotEmpty()) {
            this[type] = ids
        }
    }
}
