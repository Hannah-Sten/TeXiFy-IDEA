package nl.hannahsten.texifyidea.run.latex.ui.compiler

import com.intellij.openapi.ui.ComboBox
import com.intellij.openapi.ui.TextComponentAccessor
import com.intellij.openapi.util.io.FileUtil
import com.intellij.ui.SortedComboBoxModel

class LatexCompilerComboBoxTextComponentAccessor : TextComponentAccessor<ComboBox<LatexCompilerComboBoxItem>> {

    companion object {
        val INSTANCE = LatexCompilerComboBoxTextComponentAccessor()
    }

    override fun getText(component: ComboBox<LatexCompilerComboBoxItem>) = component.item?.presentableText ?: ""

    override fun setText(component: ComboBox<LatexCompilerComboBoxItem>, text: String) {
        val item = CustomCompilerItem(FileUtil.toSystemIndependentName(text))
        (component.model as SortedComboBoxModel<LatexCompilerComboBoxItem>).add(item)
        component.item = item
    }
}