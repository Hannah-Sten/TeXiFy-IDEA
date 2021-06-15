package nl.hannahsten.texifyidea.run.ui.compiler

import com.intellij.openapi.util.io.FileUtil
import com.intellij.util.PathUtil
import nl.hannahsten.texifyidea.run.compiler.CustomCompiler
import nl.hannahsten.texifyidea.run.step.CompileStep

/**
 * LaTeX compiler selector item that was created by the user (through first selecting [AddCompilerItem]).
 *
 * @author Sten Wessel
 */
class CustomCompilerItem<S : CompileStep>(val compiler: CustomCompiler<S>) : CompilerComboBoxItem {

    override val presentableText = FileUtil.toSystemIndependentName(compiler.executablePath)
    override val command = PathUtil.getFileName(presentableText)

    override val order = 1
}
