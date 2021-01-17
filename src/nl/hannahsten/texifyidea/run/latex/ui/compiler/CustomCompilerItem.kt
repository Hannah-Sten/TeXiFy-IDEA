package nl.hannahsten.texifyidea.run.latex.ui.compiler

import com.intellij.icons.AllIcons
import com.intellij.ui.SimpleColoredComponent
import com.intellij.util.ui.EmptyIcon

/**
 * LaTeX compiler selector item that was created by the user (through first selecting [AddCompilerItem]).
 *
 * @author Sten Wessel
 */
class CustomCompilerItem(override val presentableText: String) : LatexCompilerComboBoxItem {

    override val order = 1

    override fun render(component: SimpleColoredComponent, selected: Boolean) {
        component.append(presentableText)
        component.icon = AllIcons.FileTypes.Custom
    }

}
