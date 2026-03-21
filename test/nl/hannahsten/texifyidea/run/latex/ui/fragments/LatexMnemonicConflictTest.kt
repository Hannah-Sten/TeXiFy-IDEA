package nl.hannahsten.texifyidea.run.latex.ui.fragments

import com.intellij.testFramework.fixtures.BasePlatformTestCase
import nl.hannahsten.texifyidea.run.latex.BibtexStepOptions
import nl.hannahsten.texifyidea.run.latex.ExternalToolStepOptions
import nl.hannahsten.texifyidea.run.latex.LatexCompileStepOptions
import nl.hannahsten.texifyidea.run.latex.LatexmkCompileStepOptions
import nl.hannahsten.texifyidea.run.latex.MakeglossariesStepOptions
import nl.hannahsten.texifyidea.run.latex.MakeindexStepOptions
import nl.hannahsten.texifyidea.run.latex.PdfViewerStepOptions
import nl.hannahsten.texifyidea.run.latex.PythontexStepOptions
import nl.hannahsten.texifyidea.run.latex.XindyStepOptions
import java.awt.Component
import java.awt.Container
import java.awt.event.KeyEvent
import javax.swing.AbstractButton
import javax.swing.JLabel

class LatexMnemonicConflictTest : BasePlatformTestCase() {

    fun testCommonFieldsUseUniqueMnemonicsAndAvoidModifyOptionsMnemonic() {
        val entries = commonMnemonicEntries()

        assertUnique(entries, "common fields")
        assertNoReservedModifyOptionsMnemonic(entries, "common fields")
    }

    fun testCurrentStepScopesUseUniqueMnemonicsAndAvoidModifyOptionsMnemonic() {
        val commonEntries = commonMnemonicEntries()
        val cases = listOf(
            "compile" to LatexCompileStepFragmentedEditor(project, LatexCompileStepOptions()).component,
            "latexmk" to LatexmkStepFragmentedEditor(LatexmkCompileStepOptions()).component,
            "bibliography" to BibtexStepFragmentedEditor(project, BibtexStepOptions()).component,
            "makeindex" to MakeindexStepFragmentedEditor(project, MakeindexStepOptions()).component,
            "viewer" to LatexViewerStepFragmentedEditor(PdfViewerStepOptions()).component,
            "external tool" to ExternalToolStepFragmentedEditor(project, ExternalToolStepOptions()).component,
            "pythontex" to PythontexStepFragmentedEditor(project, PythontexStepOptions()).component,
            "makeglossaries" to MakeglossariesStepFragmentedEditor(project, MakeglossariesStepOptions()).component,
            "xindy" to XindyStepFragmentedEditor(project, XindyStepOptions()).component,
        )

        cases.forEach { (name, component) ->
            val entries = commonEntries + mnemonicEntries(component)
            assertUnique(entries, "common fields + $name step")
            assertNoReservedModifyOptionsMnemonic(entries, "common fields + $name step")
        }
    }

    private fun commonMnemonicEntries(): List<MnemonicEntry> {
        val (_, mainFileFragment) = LatexBasicFragments.createMainFileFragment("Common settings", project)
        val distributionFragment = LatexBasicFragments.createLatexDistributionFragment("Common settings", project)
        val workingDirectoryFragment = LatexBasicFragments.createWorkingDirectoryFragment("Common settings", project)
        val outputDirectoryFragment = LatexBasicFragments.createOutputDirectoryFragment("Common settings", project)
        val auxiliaryDirectoryFragment = LatexBasicFragments.createAuxiliaryDirectoryFragment("Common settings", project)

        return listOf(
            mainFileFragment.component,
            distributionFragment.component,
            workingDirectoryFragment.component,
            outputDirectoryFragment.component,
            auxiliaryDirectoryFragment.component,
        ).flatMap(::mnemonicEntries)
    }

    private fun mnemonicEntries(root: Component): List<MnemonicEntry> {
        val entries = mutableListOf<MnemonicEntry>()

        fun visit(component: Component) {
            when (component) {
                is JLabel -> {
                    if (component.displayedMnemonic != 0) {
                        entries += MnemonicEntry(normalizedText(component.text), component.displayedMnemonic)
                    }
                }
                is AbstractButton -> {
                    if (component.mnemonic != 0) {
                        entries += MnemonicEntry(normalizedText(component.text), component.mnemonic)
                    }
                }
            }
            if (component is Container) {
                component.components.forEach(::visit)
            }
        }

        visit(root)
        return entries
    }

    private fun assertUnique(entries: List<MnemonicEntry>, scope: String) {
        val duplicates = entries.groupBy { it.keyCode }.filterValues { it.size > 1 }
        assertTrue(
            "Expected unique mnemonics in $scope, found duplicates: ${
                duplicates.entries.joinToString { "${KeyEvent.getKeyText(it.key)} -> ${it.value.map(MnemonicEntry::label)}" }
            }",
            duplicates.isEmpty()
        )
    }

    private fun assertNoReservedModifyOptionsMnemonic(entries: List<MnemonicEntry>, scope: String) {
        assertFalse(
            "Expected no custom field mnemonic to reuse Modify options mnemonic M in $scope: ${entries.map(MnemonicEntry::label)}",
            entries.any { it.keyCode == KeyEvent.VK_M }
        )
    }

    private fun normalizedText(text: String?): String = text?.removeSuffix(":")?.trim().orEmpty()

    private data class MnemonicEntry(val label: String, val keyCode: Int)
}
