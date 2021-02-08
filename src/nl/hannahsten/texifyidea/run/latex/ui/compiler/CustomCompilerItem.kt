package nl.hannahsten.texifyidea.run.latex.ui.compiler

import com.intellij.openapi.util.io.FileUtil
import com.intellij.util.PathUtil
import nl.hannahsten.texifyidea.run.compiler.CustomCompiler
import nl.hannahsten.texifyidea.run.step.LatexCompileStep

/**
 * LaTeX compiler selector item that was created by the user (through first selecting [AddCompilerItem]).
 *
 * @author Sten Wessel
 */
class CustomCompilerItem<S : LatexCompileStep>(val compiler: CustomCompiler<S>) : CompilerComboBoxItem {

    override val presentableText = FileUtil.toSystemIndependentName(compiler.executablePath)
    override val command = PathUtil.getFileName(presentableText)

    override val order = 1

}
