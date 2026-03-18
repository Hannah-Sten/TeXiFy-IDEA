package nl.hannahsten.texifyidea.run.latex.ui.fragments

import com.intellij.execution.ui.CommonParameterFragments
import com.intellij.execution.ui.SettingsEditorFragment
import com.intellij.openapi.ui.ComboBox
import com.intellij.openapi.ui.LabeledComponent
import com.intellij.ui.components.JBCheckBox
import com.intellij.ui.components.JBTextField
import nl.hannahsten.texifyidea.run.latex.PdfViewerStepOptions
import nl.hannahsten.texifyidea.run.latex.StepUiOptionIds
import nl.hannahsten.texifyidea.run.pdfviewer.CustomPdfViewer
import nl.hannahsten.texifyidea.run.pdfviewer.PdfViewer
import java.awt.event.ItemEvent
import java.awt.event.KeyEvent

internal class LatexViewerStepFragmentedEditor(
    initialStep: PdfViewerStepOptions = PdfViewerStepOptions(),
) : AbstractStepFragmentedEditor<PdfViewerStepOptions>(initialStep) {

    private val viewerChoices: List<PdfViewer> = PdfViewer.availableViewers + CustomPdfViewer
    private val pdfViewer = ComboBox(viewerChoices.toTypedArray())
    private val pdfViewerRow = LabeledComponent.create(pdfViewer, "PDF &viewer")

    private val requireFocus = JBCheckBox("Require focus").apply {
        mnemonic = KeyEvent.VK_R
        displayedMnemonicIndex = 0
    }
    private val viewerCommand = JBTextField()
    private val viewerCommandRow = LabeledComponent.create(viewerCommand, "Custom viewer &command")

    init {
        pdfViewer.addItemListener {
            if (it.stateChange == ItemEvent.SELECTED) {
                updateUiState()
                fireEditorStateChanged()
            }
        }
        updateUiState()
    }

    override fun createFragments(): Collection<SettingsEditorFragment<PdfViewerStepOptions, *>> {
        val headerFragment = CommonParameterFragments.createHeader<PdfViewerStepOptions>("PDF Viewer Step")

        val viewerFragment = stepFragment(
            id = "step.viewer.type",
            name = "PDF viewer",
            component = pdfViewerRow,
            reset = { step, component ->
                component.component.selectedItem = selectedViewerFor(step)
                updateUiState()
            },
            apply = { step, component ->
                val selectedViewer = component.component.selectedItem as? PdfViewer ?: PdfViewer.firstAvailableViewer
                if (selectedViewer == CustomPdfViewer) {
                    step.pdfViewerName = CustomPdfViewer.name
                    step.customViewerCommand = viewerCommand.text.ifBlank { null }
                }
                else {
                    step.pdfViewerName = selectedViewer.name
                    step.customViewerCommand = null
                }
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
            apply = { step, component ->
                step.customViewerCommand = if (isCustomViewerSelected()) {
                    component.component.text.ifBlank { null }
                }
                else {
                    null
                }
            },
            initiallyVisible = { step -> !step.customViewerCommand.isNullOrBlank() || step.pdfViewerName == CustomPdfViewer.name },
            removable = false,
            hint = "Command template used when PDF viewer is set to Custom viewer.",
        )

        return listOf(
            headerFragment,
            viewerFragment,
            focusFragment,
            commandFragment,
        )
    }

    private fun selectedViewerFor(step: PdfViewerStepOptions): PdfViewer {
        if (!step.customViewerCommand.isNullOrBlank() || step.pdfViewerName == CustomPdfViewer.name) {
            return CustomPdfViewer
        }
        return viewerChoices.firstOrNull { it.name == step.pdfViewerName }
            ?: PdfViewer.firstAvailableViewer
    }

    private fun isCustomViewerSelected(): Boolean = pdfViewer.selectedItem == CustomPdfViewer

    private fun updateUiState() {
        viewerCommandRow.isVisible = isCustomViewerSelected()
        updateRequireFocusEnabled()
    }

    private fun updateRequireFocusEnabled() {
        val selectedViewer = pdfViewer.selectedItem as? PdfViewer
        val supported = selectedViewer?.let { it.isForwardSearchSupported && it.isFocusSupported } ?: false
        requireFocus.isEnabled = supported
    }
}
