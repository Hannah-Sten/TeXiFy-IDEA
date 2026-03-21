package nl.hannahsten.texifyidea.run.latex.ui.fragments

import com.intellij.testFramework.fixtures.BasePlatformTestCase
import java.awt.Component
import java.awt.Container
import java.awt.event.KeyEvent
import javax.swing.JLabel

class LatexBasicFragmentsMnemonicTest : BasePlatformTestCase() {

    fun testCommonFragmentsExposeMnemonics() {
        val (_, mainFileFragment) = LatexBasicFragments.createMainFileFragment("Common settings", project)
        val distributionFragment = LatexBasicFragments.createLatexDistributionFragment("Common settings", project)
        val workingDirectoryFragment = LatexBasicFragments.createWorkingDirectoryFragment("Common settings", project)
        val outputDirectoryFragment = LatexBasicFragments.createOutputDirectoryFragment("Common settings", project)
        val auxiliaryDirectoryFragment = LatexBasicFragments.createAuxiliaryDirectoryFragment("Common settings", project)

        assertLabelMnemonic(mainFileFragment.component, "Main file", KeyEvent.VK_F)
        assertLabelMnemonic(distributionFragment.component, "LaTeX distribution", KeyEvent.VK_D)
        assertLabelMnemonic(workingDirectoryFragment.component, "Working directory", KeyEvent.VK_W)
        assertLabelMnemonic(outputDirectoryFragment.component, "Output directory", KeyEvent.VK_O)
        assertLabelMnemonic(auxiliaryDirectoryFragment.component, "Auxiliary directory", KeyEvent.VK_U)
    }

    private fun assertLabelMnemonic(root: Component, expectedText: String, expectedMnemonic: Int) {
        val label = findLabels(root).firstOrNull { normalizedText(it.text) == expectedText }
        assertNotNull("Expected label '$expectedText' to exist", label)
        assertEquals(expectedMnemonic, label!!.displayedMnemonic)
        assertTrue(label.displayedMnemonicIndex >= 0)
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

    private fun normalizedText(text: String?): String? = text?.removeSuffix(":")?.trim()
}
