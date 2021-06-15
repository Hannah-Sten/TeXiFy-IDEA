package nl.hannahsten.texifyidea.run.ui.compiler

import com.intellij.openapi.ui.ComboBox
import com.intellij.openapi.ui.TextComponentAccessor
import com.intellij.openapi.util.io.FileUtil
import com.intellij.ui.SortedComboBoxModel
import nl.hannahsten.texifyidea.run.compiler.latex.CustomLatexCompiler

class LatexCompilerComboBoxTextComponentAccessor : TextComponentAccessor<ComboBox<CompilerComboBoxItem>> {

    companion object {

        val INSTANCE = LatexCompilerComboBoxTextComponentAccessor()
    }

    override fun getText(component: ComboBox<CompilerComboBoxItem>) = component.item?.presentableText ?: ""

    override fun setText(component: ComboBox<CompilerComboBoxItem>, text: String) {
        val item = CustomCompilerItem(CustomLatexCompiler(FileUtil.toSystemIndependentName(text)))
        (component.model as SortedComboBoxModel<CompilerComboBoxItem>).add(item)
        component.item = item
    }
}