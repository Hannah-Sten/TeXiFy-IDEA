package nl.hannahsten.texifyidea.run.latex.ui.fragments

import com.intellij.execution.ui.CommonParameterFragments
import com.intellij.execution.ui.SettingsEditorFragment
import com.intellij.openapi.util.text.TextWithMnemonic
import com.intellij.openapi.ui.ComboBox
import com.intellij.openapi.ui.LabeledComponent
import com.intellij.ui.components.JBCheckBox
import com.intellij.ui.components.JBTextField
import nl.hannahsten.texifyidea.TexifyBundle
import nl.hannahsten.texifyidea.run.latex.PdfViewerStepOptions
import nl.hannahsten.texifyidea.run.latex.StepUiOptionIds
import nl.hannahsten.texifyidea.run.pdfviewer.CustomPdfViewer
import nl.hannahsten.texifyidea.run.pdfviewer.PdfViewer
import java.awt.event.ItemEvent

internal class LatexViewerStepFragmentedEditor(
    initialStep: PdfViewerStepOptions = PdfViewerStepOptions(),
) : AbstractStepFragmentedEditor<PdfViewerStepOptions>(initialStep) {

    private val viewerChoices: List<PdfViewer> = PdfViewer.availableViewers + CustomPdfViewer
    private val pdfViewer = ComboBox(viewerChoices.toTypedArray())
    private val pdfViewerRow = LabeledComponent.create(pdfViewer, TexifyBundle.message("run.step.ui.field.pdf.viewer.label"))

    private val requireFocus = JBCheckBox().apply {
        setTextWithMnemonic(TexifyBundle.message("run.step.ui.field.require.focus.label"))
    }
    private val viewerCommand = JBTextField()
    private val viewerCommandRow = LabeledComponent.create(viewerCommand, TexifyBundle.message("run.step.ui.field.custom.viewer.command.label"))

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
        val headerFragment = CommonParameterFragments.createHeader<PdfViewerStepOptions>(TexifyBundle.message("run.step.ui.header.pdf.viewer"))

        val viewerFragment = stepFragment(
            id = "step.viewer.type",
            name = TexifyBundle.message("run.step.ui.field.pdf.viewer"),
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
            hint = TexifyBundle.message("run.step.ui.hint.pdf.viewer"),
        )

        val focusFragment = stepFragment(
            id = StepUiOptionIds.VIEWER_REQUIRE_FOCUS,
            name = TexifyBundle.message("run.step.ui.field.require.focus"),
            component = requireFocus,
            reset = { step, component ->
                component.isSelected = step.requireFocus
                updateRequireFocusEnabled()
            },
            apply = { step, component ->
                step.requireFocus = component.isSelected
            },
            initiallyVisible = { step -> step.requireFocus },
            removable = true,
            hint = TexifyBundle.message("run.step.ui.hint.require.focus"),
            actionHint = TexifyBundle.message("run.step.ui.action.set.require.focus"),
        )

        val commandFragment = stepFragment(
            id = StepUiOptionIds.VIEWER_COMMAND,
            name = TexifyBundle.message("run.step.ui.field.custom.viewer.command"),
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
            hint = TexifyBundle.message("run.step.ui.hint.custom.viewer.command"),
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

    internal fun setValuesForTest(
        pdfViewerName: String,
        customViewerCommand: String?,
    ) {
        pdfViewer.selectedItem = viewerChoices.firstOrNull { it.name == pdfViewerName }
            ?: CustomPdfViewer.takeIf { pdfViewerName == it.name }
            ?: PdfViewer.firstAvailableViewer
        viewerCommand.text = customViewerCommand.orEmpty()
        updateUiState()
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

    private fun JBCheckBox.setTextWithMnemonic(value: String) {
        val mnemonicText = TextWithMnemonic.fromMnemonicText(value) ?: TextWithMnemonic.parse(value)
        text = mnemonicText.text
        mnemonic = mnemonicText.mnemonicCode
        displayedMnemonicIndex = mnemonicText.mnemonicIndex
    }
}
