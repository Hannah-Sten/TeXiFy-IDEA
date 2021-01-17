package nl.hannahsten.texifyidea.run.latex.ui.compiler

import com.intellij.ui.SimpleColoredComponent

/**
 * @author Sten Wessel
 */
interface LatexCompilerComboBoxItem {

    val presentableText: String

    val id: String?
        get() = null

    val order: Int

    fun render(component: SimpleColoredComponent, selected: Boolean)
}