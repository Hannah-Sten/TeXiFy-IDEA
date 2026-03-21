package nl.hannahsten.texifyidea.run.latex.ui.fragments

import com.intellij.testFramework.fixtures.BasePlatformTestCase
import nl.hannahsten.texifyidea.run.latex.BibtexStepOptions
import nl.hannahsten.texifyidea.run.latex.LatexmkCompileStepOptions
import nl.hannahsten.texifyidea.run.latex.MakeindexStepOptions
import nl.hannahsten.texifyidea.run.latex.PdfViewerStepOptions
import java.awt.Component
import java.awt.Container
import java.awt.event.KeyEvent
import javax.swing.AbstractButton
import javax.swing.JLabel

class LatexStepFieldMnemonicTest : BasePlatformTestCase() {

    fun testCompileStepFieldsExposeMnemonics() {
        assertLabelMnemonic(LatexCompileStepFragmentedEditor(project).component, "Compiler", KeyEvent.VK_C)
        assertLabelMnemonic(LatexCompileStepFragmentedEditor(project).component, "Compiler path", KeyEvent.VK_P)
        assertLabelMnemonic(LatexCompileStepFragmentedEditor(project).component, "Compiler arguments", KeyEvent.VK_G)
        assertLabelMnemonic(LatexCompileStepFragmentedEditor(project).component, "Output format", KeyEvent.VK_T)
    }

    fun testLatexmkStepFieldsExposeMnemonics() {
        val component = LatexmkStepFragmentedEditor(LatexmkCompileStepOptions()).component

        assertLabelMnemonic(component, "Compiler path", KeyEvent.VK_P)
        assertLabelMnemonic(component, "Compile mode", KeyEvent.VK_E)
        assertLabelMnemonic(component, "Custom engine command", KeyEvent.VK_N)
        assertLabelMnemonic(component, "Citation tool", KeyEvent.VK_T)
        assertLabelMnemonic(component, "Extra arguments", KeyEvent.VK_G)
    }

    fun testBibliographyStepFieldsExposeMnemonics() {
        val component = BibtexStepFragmentedEditor(project, BibtexStepOptions()).component

        assertLabelMnemonic(component, "Bibliography tool", KeyEvent.VK_B)
        assertLabelMnemonic(component, "Compiler path", KeyEvent.VK_P)
        assertLabelMnemonic(component, "Compiler arguments", KeyEvent.VK_G)
        assertLabelMnemonic(component, "Working directory", KeyEvent.VK_I)
    }

    fun testMakeindexStepFieldsExposeMnemonics() {
        val component = MakeindexStepFragmentedEditor(project, MakeindexStepOptions()).component

        assertLabelMnemonic(component, "Program", KeyEvent.VK_P)
        assertLabelMnemonic(component, "Program arguments", KeyEvent.VK_G)
        assertLabelMnemonic(component, "Target base name", KeyEvent.VK_N)
        assertLabelMnemonic(component, "Working directory", KeyEvent.VK_I)
    }

    fun testViewerStepFieldsExposeMnemonics() {
        val component = LatexViewerStepFragmentedEditor(PdfViewerStepOptions()).component

        assertLabelMnemonic(component, "PDF viewer", KeyEvent.VK_V)
        assertButtonMnemonic(component, "Require focus", KeyEvent.VK_R)
        assertLabelMnemonic(component, "Custom viewer command", KeyEvent.VK_C)
    }

    private fun assertLabelMnemonic(root: Component, expectedText: String, expectedMnemonic: Int) {
        val label = findLabels(root).firstOrNull { normalizedText(it.text) == expectedText }
        assertNotNull("Expected label '$expectedText' to exist", label)
        assertEquals(expectedMnemonic, label!!.displayedMnemonic)
        assertTrue(label.displayedMnemonicIndex >= 0)
    }

    private fun assertButtonMnemonic(root: Component, expectedText: String, expectedMnemonic: Int) {
        val button = findButtons(root).firstOrNull { normalizedText(it.text) == expectedText }
        assertNotNull("Expected button '$expectedText' to exist", button)
        assertEquals(expectedMnemonic, button!!.mnemonic)
        assertTrue(button.displayedMnemonicIndex >= 0)
    }

    private fun findLabels(root: Component): List<JLabel> {
        val matches = mutableListOf<JLabel>()

        fun visit(component: Component) {
            if (component is JLabel) {
                matches.add(component)
            }
            if (component is Container) {
                component.components.forEach(::visit)
            }
        }

        visit(root)
        return matches
    }

    private fun findButtons(root: Component): List<AbstractButton> {
        val matches = mutableListOf<AbstractButton>()

        fun visit(component: Component) {
            if (component is AbstractButton) {
                matches.add(component)
            }
            if (component is Container) {
                component.components.forEach(::visit)
            }
        }

        visit(root)
        return matches
    }

    private fun normalizedText(text: String?): String? = text?.removeSuffix(":")?.trim()
}
