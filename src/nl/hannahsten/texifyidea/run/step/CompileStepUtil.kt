package nl.hannahsten.texifyidea.run.step

import com.intellij.execution.ui.CommonParameterFragments
import com.intellij.ide.macro.MacrosDialog
import com.intellij.ui.components.fields.ExpandableTextField
import com.intellij.util.ui.JBDimension
import nl.hannahsten.texifyidea.run.executable.Executable
import nl.hannahsten.texifyidea.run.ui.compiler.ExecutableEditor

/**
 * Some UI properties common to multiple steps.
 */
internal fun <E : Executable> setDefaultLayout(editor: ExecutableEditor<*, E>, selected: E?) {
    editor.apply {
        CommonParameterFragments.setMonospaced(component)
        minimumSize = JBDimension(200, 30)
        label.isVisible = false
        component.setMinimumAndPreferredWidth(150)

        setSelectedExecutable(selected)
    }
}

internal fun createParametersTextField(type: String, arguments: String?): ExpandableTextField = ExpandableTextField().apply {
    emptyText.text = "$type arguments"
    MacrosDialog.addMacroSupport(this, MacrosDialog.Filters.ALL) { false }
    CommonParameterFragments.setMonospaced(this)

    text = arguments
}