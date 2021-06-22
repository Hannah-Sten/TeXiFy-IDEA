package nl.hannahsten.texifyidea.run.ui.compiler

import com.intellij.openapi.ui.ComboBox
import com.intellij.openapi.ui.TextComponentAccessor
import com.intellij.openapi.util.io.FileUtil
import com.intellij.ui.SortedComboBoxModel
import nl.hannahsten.texifyidea.run.executable.CustomExecutable

class ExecutableComboBoxTextComponentAccessor(val compilerCreator: (String) -> CustomExecutable) : TextComponentAccessor<ComboBox<ExecutableComboBoxItem>> {

    override fun getText(component: ComboBox<ExecutableComboBoxItem>) = component.item?.presentableText ?: ""

    override fun setText(component: ComboBox<ExecutableComboBoxItem>, text: String) {

        val item = CustomExecutableItem(compilerCreator(FileUtil.toSystemIndependentName(text)))
        (component.model as SortedComboBoxModel<ExecutableComboBoxItem>).add(item)
        component.item = item
    }
}