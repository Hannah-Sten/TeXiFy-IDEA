package nl.hannahsten.texifyidea.run.latex.ui.fragments

import com.intellij.openapi.ui.ComboBox
import com.intellij.openapi.ui.LabeledComponent
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import com.intellij.ui.components.JBTextField
import nl.hannahsten.texifyidea.run.latex.PdfViewerStepOptions
import nl.hannahsten.texifyidea.run.pdfviewer.CustomPdfViewer
import nl.hannahsten.texifyidea.run.pdfviewer.PdfViewer
import java.awt.Component
import java.awt.Container
import javax.swing.JLabel

class LatexViewerStepFragmentedEditorTest : BasePlatformTestCase() {

    fun testResetSelectsCustomViewerWhenCustomCommandConfigured() {
        val editor = LatexViewerStepFragmentedEditor()
        val step = PdfViewerStepOptions().apply {
            pdfViewerName = PdfViewer.firstAvailableViewer.name
            customViewerCommand = "open {pdf}"
        }

        editor.resetFrom(step)

        assertEquals(CustomPdfViewer, viewerCombo(editor).selectedItem)
        assertTrue(viewerCommandRow(editor).isVisible)
        assertEquals("open {pdf}", viewerCommandField(editor).text)
    }

    fun testApplyStoresCustomViewerSelectionAndCommand() {
        val editor = LatexViewerStepFragmentedEditor()
        val step = PdfViewerStepOptions()

        editor.resetFrom(step)
        viewerCombo(editor).selectedItem = CustomPdfViewer
        viewerCommandField(editor).text = "open {pdf}"

        editor.applyTo(step)

        assertEquals(CustomPdfViewer.name, step.pdfViewerName)
        assertEquals("open {pdf}", step.customViewerCommand)
    }

    fun testApplyNormalViewerClearsCustomViewerCommand() {
        val editor = LatexViewerStepFragmentedEditor()
        val realViewer = PdfViewer.firstAvailableViewer
        val step = PdfViewerStepOptions().apply {
            pdfViewerName = CustomPdfViewer.name
            customViewerCommand = "open {pdf}"
        }

        editor.resetFrom(step)
        viewerCombo(editor).selectedItem = realViewer

        editor.applyTo(step)

        assertEquals(realViewer.name, step.pdfViewerName)
        assertNull(step.customViewerCommand)
        assertFalse(viewerCommandRow(editor).isVisible)
    }

    @Suppress("UNCHECKED_CAST")
    private fun viewerCombo(editor: LatexViewerStepFragmentedEditor): ComboBox<PdfViewer> = findComponents(editor.component, ComboBox::class.java).single() as ComboBox<PdfViewer>

    private fun viewerCommandField(editor: LatexViewerStepFragmentedEditor): JBTextField = findComponents(editor.component, JBTextField::class.java).single()

    private fun viewerCommandRow(editor: LatexViewerStepFragmentedEditor): LabeledComponent<JBTextField> {
        val label = findComponents(editor.component, JLabel::class.java)
            .firstOrNull { normalizedText(it.text) == "Custom viewer command" }
        assertNotNull("Expected label 'Custom viewer command' to exist", label)

        var current: Component? = label
        while (current != null && current !is LabeledComponent<*>) {
            current = current.parent
        }
        assertTrue(current is LabeledComponent<*>)

        @Suppress("UNCHECKED_CAST")
        return current as LabeledComponent<JBTextField>
    }

    private fun <T : Component> findComponents(root: Component, type: Class<T>): List<T> {
        val matches = mutableListOf<T>()

        val stack = ArrayDeque<Component>()
        stack.add(root)
        while (stack.isNotEmpty()) {
            val component = stack.removeLast()
            if (type.isInstance(component)) {
                matches += type.cast(component)
            }
            if (component is Container) {
                component.components.forEach(stack::add)
            }
        }
        return matches
    }

    private fun normalizedText(text: String?): String? = text?.removeSuffix(":")?.trim()
}
