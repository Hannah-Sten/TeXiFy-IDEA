package nl.hannahsten.texifyidea.run.latex.ui.fragments

import com.intellij.execution.ui.CommonParameterFragments
import com.intellij.execution.ui.SettingsEditorFragment
import com.intellij.openapi.ui.ComboBox
import com.intellij.openapi.ui.LabeledComponent
import com.intellij.ui.components.JBCheckBox
import com.intellij.ui.components.JBTextField
import nl.hannahsten.texifyidea.run.latex.PdfViewerStepOptions
import nl.hannahsten.texifyidea.run.latex.StepUiOptionIds
import nl.hannahsten.texifyidea.run.pdfviewer.PdfViewer
import java.awt.event.ItemEvent

internal class LatexViewerStepFragmentedEditor(
    initialStep: PdfViewerStepOptions = PdfViewerStepOptions(),
) : AbstractStepFragmentedEditor<PdfViewerStepOptions>(initialStep) {

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

    override fun createFragments(): Collection<SettingsEditorFragment<PdfViewerStepOptions, *>> {
        val headerFragment = CommonParameterFragments.createHeader<PdfViewerStepOptions>("PDF Viewer Step")

        val viewerFragment = stepFragment(
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

        val focusFragment = stepFragment(
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

        val commandFragment = stepFragment(
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
}
