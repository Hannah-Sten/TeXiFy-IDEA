package nl.hannahsten.texifyidea.run.latex.ui.compiler

import com.intellij.ui.SimpleColoredComponent
import com.intellij.util.ui.EmptyIcon

/**
 * LaTeX compiler selector item which allows the user to select a custom executable.
 *
 * @author Sten Wessel
 */
class AddCompilerItem : LatexCompilerComboBoxItem {

    override val presentableText = "Select alternative compiler..."
    override val order = Int.MAX_VALUE

    override fun render(component: SimpleColoredComponent, selected: Boolean) {
        component.append(presentableText)
        component.icon = EmptyIcon.ICON_16
    }

}
