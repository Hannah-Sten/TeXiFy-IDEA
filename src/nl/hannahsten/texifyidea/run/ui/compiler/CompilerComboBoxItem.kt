package nl.hannahsten.texifyidea.run.ui.compiler

import com.intellij.ui.SimpleColoredComponent
import com.intellij.ui.SimpleTextAttributes

/**
 * @author Sten Wessel
 */
interface CompilerComboBoxItem {

    val presentableText: String
    val command: String

    val order: Int

    fun render(component: SimpleColoredComponent, selected: Boolean) {
        if (selected) {
            component.append("$command ")
            component.append(presentableText, SimpleTextAttributes.GRAYED_SMALL_ATTRIBUTES)
        }
        else {
            component.append(presentableText)
        }
    }
}