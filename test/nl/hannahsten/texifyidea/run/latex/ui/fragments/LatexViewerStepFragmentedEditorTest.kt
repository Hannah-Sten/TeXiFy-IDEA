package nl.hannahsten.texifyidea.run.latex.ui.fragments

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.ui.ComboBox
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import com.intellij.ui.components.JBTextField
import nl.hannahsten.texifyidea.run.latex.PdfViewerStepOptions
import nl.hannahsten.texifyidea.run.pdfviewer.CustomPdfViewer
import nl.hannahsten.texifyidea.run.pdfviewer.PdfViewer
import java.awt.Component
import java.awt.Container

class LatexViewerStepFragmentedEditorTest : BasePlatformTestCase() {

    fun testResetSelectsCustomViewerWhenCustomCommandConfigured() {
        val editor = LatexViewerStepFragmentedEditor()
        val step = PdfViewerStepOptions().apply {
            pdfViewerName = PdfViewer.firstAvailableViewer.name
            customViewerCommand = "open {pdf}"
        }

        editor.resetFrom(step)

        assertEquals(CustomPdfViewer, viewerCombo(editor).selectedItem)
        assertTrue(isEffectivelyVisible(viewerCommandField(editor)))
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
        val step = PdfViewerStepOptions().apply {
            pdfViewerName = CustomPdfViewer.name
            customViewerCommand = "open {pdf}"
        }

        editor.resetFrom(step)
        val combo = viewerCombo(editor)
        val realViewer = (0 until combo.itemCount)
            .map(combo::getItemAt)
            .first { it != CustomPdfViewer }
        combo.selectedItem = realViewer
        drainEdt()
        assertFalse(visibilityState(editor), isEffectivelyVisible(viewerCommandField(editor)))

        editor.applyTo(step)

        assertEquals(realViewer.name, step.pdfViewerName)
        assertNull(step.customViewerCommand)
        assertFalse(visibilityState(editor), isEffectivelyVisible(viewerCommandField(editor)))
    }

    @Suppress("UNCHECKED_CAST")
    private fun viewerCombo(editor: LatexViewerStepFragmentedEditor): ComboBox<PdfViewer> = findComponents(editor.component, ComboBox::class.java).single() as ComboBox<PdfViewer>

    private fun viewerCommandField(editor: LatexViewerStepFragmentedEditor): JBTextField = findComponents(editor.component, JBTextField::class.java).single()

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

    private fun drainEdt() {
        ApplicationManager.getApplication().invokeAndWait {}
    }

    private fun isEffectivelyVisible(component: Component): Boolean {
        var current: Component? = component
        while (current != null) {
            if (!current.isVisible) {
                return false
            }
            current = current.parent
        }
        return true
    }

    /**
     * Print layout properties for debugging purposes.
     */
    private fun visibilityState(editor: LatexViewerStepFragmentedEditor): String {
        val chain = mutableListOf<String>()
        var current: Component? = viewerCommandField(editor)
        while (current != null) {
            chain += "${current::class.java.simpleName}:${current.isVisible}"
            current = current.parent
        }
        return chain.joinToString(" -> ")
    }
}
