package nl.hannahsten.texifyidea.run.latex.ui.fragments

import com.intellij.execution.ui.FragmentedSettingsEditor
import com.intellij.execution.ui.SettingsEditorFragment
import com.intellij.openapi.ui.ComboBox
import com.intellij.openapi.ui.LabeledComponent
import com.intellij.ui.components.JBCheckBox
import com.intellij.ui.components.JBTextField
import nl.hannahsten.texifyidea.run.latex.LatexRunConfiguration
import nl.hannahsten.texifyidea.run.latex.StepUiOptionIds
import nl.hannahsten.texifyidea.run.pdfviewer.PdfViewer
import java.awt.event.ItemEvent
import java.util.function.BiConsumer
import java.util.function.Predicate
import javax.swing.JComponent

internal class LatexViewerStepFragmentedEditor(
    state: StepFragmentedState = StepFragmentedState(),
) : FragmentedSettingsEditor<StepFragmentedState>(state) {

    private val pdfViewer = ComboBox(PdfViewer.availableViewers.toTypedArray())
    private val pdfViewerRow = LabeledComponent.create(pdfViewer, "PDF viewer")

    private val requireFocus = JBCheckBox("Allow PDF viewer to focus after compilation")
    private val viewerCommand = JBTextField()
    private val viewerCommandRow = LabeledComponent.create(viewerCommand, "Custom viewer command")

    init {
        pdfViewer.addItemListener {
            if (it.stateChange == ItemEvent.SELECTED) {
                updateRequireFocusEnabled()
                fireEditorStateChanged()
            }
        }
    }

    override fun createFragments(): Collection<SettingsEditorFragment<StepFragmentedState, *>> {
        val viewerFragment = fragment(
            id = "step.viewer.type",
            name = "PDF viewer",
            component = pdfViewerRow,
            reset = { runConfig, component ->
                component.component.selectedItem = runConfig.pdfViewer ?: PdfViewer.firstAvailableViewer
                updateRequireFocusEnabled()
            },
            apply = { runConfig, component ->
                runConfig.pdfViewer = component.component.selectedItem as? PdfViewer ?: PdfViewer.firstAvailableViewer
            },
            initiallyVisible = { true },
            removable = false,
        )

        val focusFragment = fragment(
            id = StepUiOptionIds.VIEWER_REQUIRE_FOCUS,
            name = "Require focus",
            component = requireFocus,
            reset = { runConfig, component ->
                component.isSelected = runConfig.requireFocus
                updateRequireFocusEnabled()
            },
            apply = { runConfig, component ->
                runConfig.requireFocus = component.isSelected
            },
            initiallyVisible = { runConfig -> !runConfig.requireFocus },
            removable = true,
            actionHint = "Set require focus",
        )

        val commandFragment = fragment(
            id = StepUiOptionIds.VIEWER_COMMAND,
            name = "Custom viewer command",
            component = viewerCommandRow,
            reset = { runConfig, component -> component.component.text = runConfig.viewerCommand.orEmpty() },
            apply = { runConfig, component -> runConfig.viewerCommand = component.component.text.ifBlank { null } },
            initiallyVisible = { runConfig -> !runConfig.viewerCommand.isNullOrBlank() },
            removable = true,
            actionHint = "Set custom viewer command",
        )

        return listOf(
            viewerFragment,
            focusFragment,
            commandFragment,
        )
    }

    private fun updateRequireFocusEnabled() {
        val selectedViewer = pdfViewer.selectedItem as? PdfViewer
        val supported = selectedViewer?.let { it.isForwardSearchSupported && it.isFocusSupported } ?: false
        requireFocus.isEnabled = supported
    }

    private fun <C : JComponent> fragment(
        id: String,
        name: String,
        component: C,
        reset: (LatexRunConfiguration, C) -> Unit,
        apply: (LatexRunConfiguration, C) -> Unit,
        initiallyVisible: (LatexRunConfiguration) -> Boolean,
        removable: Boolean,
        actionHint: String? = null,
    ): SettingsEditorFragment<StepFragmentedState, C> {
        val fragment = SettingsEditorFragment(
            id,
            name,
            null,
            component,
            0,
            BiConsumer<StepFragmentedState, C> { state, comp -> reset(state.runConfig, comp) },
            BiConsumer<StepFragmentedState, C> { state, comp -> apply(state.runConfig, comp) },
            Predicate<StepFragmentedState> { state -> initiallyVisible(state.runConfig) }
        )
        fragment.isRemovable = removable
        fragment.isCanBeHidden = removable
        actionHint?.let { fragment.actionHint = it }
        return fragment
    }
}
