package nl.hannahsten.texifyidea.run.latex.ui.compiler

import com.intellij.ui.SimpleColoredComponent
import com.intellij.util.ui.EmptyIcon

/**
 * LaTeX compiler selector item for the default compilers for which we have support built-in.
 *
 * @author Sten Wessel
 */
sealed class BuiltinCompilerItem : LatexCompilerComboBoxItem {

    override val order = 2

    override fun render(component: SimpleColoredComponent, selected: Boolean) {
        component.append(presentableText)
        component.icon = EmptyIcon.ICON_16
    }
}


class PdflatexCompilerItem : BuiltinCompilerItem() {

    override val presentableText = "pdflatex"
}
