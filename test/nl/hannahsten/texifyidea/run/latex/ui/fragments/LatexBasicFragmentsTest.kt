package nl.hannahsten.texifyidea.run.latex.ui.fragments

import com.intellij.openapi.ui.TextFieldWithBrowseButton
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import com.intellij.ui.components.fields.ExtendableTextField

class LatexBasicFragmentsTest : BasePlatformTestCase() {

    fun testOutputDirectoryFragmentUsesMacrosDialogExtension() {
        val fragment = LatexBasicFragments.createOutputDirectoryFragment("Common settings", project)
        val field = fragment.component().component as TextFieldWithBrowseButton
        val textField = field.textField as ExtendableTextField

        assertTrue(textField.extensions.isNotEmpty())
        assertFalse(textField.extensions.any { it.tooltip == "Insert path macro" })
    }
}
