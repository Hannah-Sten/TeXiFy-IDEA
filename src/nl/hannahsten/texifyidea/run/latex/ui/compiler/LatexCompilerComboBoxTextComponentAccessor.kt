package nl.hannahsten.texifyidea.run.latex.ui.compiler

import com.intellij.openapi.ui.ComboBox
import com.intellij.openapi.ui.TextComponentAccessor
import com.intellij.openapi.util.io.FileUtil
import com.intellij.ui.SortedComboBoxModel
import nl.hannahsten.texifyidea.run.compiler.CustomLatexCompiler

class LatexCompilerComboBoxTextComponentAccessor : TextComponentAccessor<ComboBox<LatexCompilerComboBoxItem>> {

    companion object {
        val INSTANCE = LatexCompilerComboBoxTextComponentAccessor()
    }

    override fun getText(component: ComboBox<LatexCompilerComboBoxItem>) = component.item?.presentableText ?: ""

    override fun setText(component: ComboBox<LatexCompilerComboBoxItem>, text: String) {
        val item = CustomCompilerItem(CustomLatexCompiler(FileUtil.toSystemIndependentName(text)))
        (component.model as SortedComboBoxModel<LatexCompilerComboBoxItem>).add(item)
        component.item = item
    }
}