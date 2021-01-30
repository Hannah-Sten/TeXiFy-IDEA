package nl.hannahsten.texifyidea.run.latex.ui.compiler

import com.intellij.icons.AllIcons
import com.intellij.openapi.util.io.FileUtil
import com.intellij.ui.SimpleColoredComponent
import com.intellij.ui.SimpleTextAttributes
import com.intellij.util.PathUtil
import com.intellij.util.ui.EmptyIcon
import nl.hannahsten.texifyidea.run.compiler.CustomLatexCompiler

/**
 * LaTeX compiler selector item that was created by the user (through first selecting [AddCompilerItem]).
 *
 * @author Sten Wessel
 */
class CustomCompilerItem(val compiler: CustomLatexCompiler) : LatexCompilerComboBoxItem {

    override val presentableText = FileUtil.toSystemIndependentName(compiler.executablePath)
    override val command = PathUtil.getFileName(presentableText)

    override val order = 1

}
