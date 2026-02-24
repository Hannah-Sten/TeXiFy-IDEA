package nl.hannahsten.texifyidea.run.latex.ui.fragments

import com.intellij.execution.ui.CommonParameterFragments
import com.intellij.execution.ui.FragmentedSettingsEditor
import com.intellij.execution.ui.SettingsEditorFragment
import com.intellij.openapi.ui.ComboBox
import com.intellij.openapi.ui.LabeledComponent
import com.intellij.ui.components.JBCheckBox
import com.intellij.ui.components.JBTextField
import nl.hannahsten.texifyidea.run.latex.PdfViewerStepOptions
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
        val headerFragment = CommonParameterFragments.createHeader<StepFragmentedState>("PDF viewer step")

        val viewerFragment = fragment(
            id = "step.viewer.type",
            name = "PDF viewer",
            component = pdfViewerRow,
            reset = { step, component ->
                component.component.selectedItem = PdfViewer.availableViewers.firstOrNull { it.name == step.pdfViewerName }
                    ?: PdfViewer.firstAvailableViewer
                updateRequireFocusEnabled()
            },
            apply = { step, component ->
                step.pdfViewerName = (component.component.selectedItem as? PdfViewer ?: PdfViewer.firstAvailableViewer).name
            },
            initiallyVisible = { true },
            removable = false,
            hint = "PDF viewer used by pdf-viewer steps.",
        )

        val focusFragment = fragment(
            id = StepUiOptionIds.VIEWER_REQUIRE_FOCUS,
            name = "Require focus",
            component = requireFocus,
            reset = { step, component ->
                component.isSelected = step.requireFocus
                updateRequireFocusEnabled()
            },
            apply = { step, component ->
                step.requireFocus = component.isSelected
            },
            initiallyVisible = { step -> !step.requireFocus },
            removable = true,
            hint = "Allow the viewer window to gain focus after compilation.",
            actionHint = "Set require focus",
        )

        val commandFragment = fragment(
            id = StepUiOptionIds.VIEWER_COMMAND,
            name = "Custom viewer command",
            component = viewerCommandRow,
            reset = { step, component -> component.component.text = step.customViewerCommand.orEmpty() },
            apply = { step, component -> step.customViewerCommand = component.component.text.ifBlank { null } },
            initiallyVisible = { step -> !step.customViewerCommand.isNullOrBlank() },
            removable = true,
            hint = "Command template used when PDF viewer is set to CUSTOM.",
            actionHint = "Set custom viewer command",
        )

        return listOf(
            headerFragment,
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
        reset: (PdfViewerStepOptions, C) -> Unit,
        apply: (PdfViewerStepOptions, C) -> Unit,
        initiallyVisible: (PdfViewerStepOptions) -> Boolean,
        removable: Boolean,
        hint: String? = null,
        actionHint: String? = null,
    ): SettingsEditorFragment<StepFragmentedState, C> {
        val fragment = SettingsEditorFragment(
            id,
            name,
            null,
            component,
            0,
            BiConsumer<StepFragmentedState, C> { state, comp ->
                withSelectedStep(state) { runConfig -> reset(runConfig, comp) }
            },
            BiConsumer<StepFragmentedState, C> { state, comp ->
                withSelectedStep(state) { runConfig -> apply(runConfig, comp) }
            },
            Predicate<StepFragmentedState> { state ->
                withSelectedStep(state) { runConfig -> initiallyVisible(runConfig) }
            }
        )
        fragment.isRemovable = removable
        fragment.isCanBeHidden = removable
        hint?.let { applyTooltip(component, it) }
        actionHint?.let { fragment.actionHint = it }
        return fragment
    }

    private fun applyTooltip(component: JComponent, tooltip: String) {
        component.toolTipText = tooltip
        if (component is LabeledComponent<*>) {
            component.component.toolTipText = tooltip
        }
    }

    private inline fun <T> withSelectedStep(state: StepFragmentedState, block: (PdfViewerStepOptions) -> T): T {
        val step = state.selectedStepOptions as? PdfViewerStepOptions
            ?: PdfViewerStepOptions().also { state.selectedStepOptions = it }
        return block(step)
    }
}
