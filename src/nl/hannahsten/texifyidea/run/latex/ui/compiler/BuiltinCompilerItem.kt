package nl.hannahsten.texifyidea.run.latex.ui.compiler

import nl.hannahsten.texifyidea.run.compiler.SupportedLatexCompiler

/**
 * LaTeX compiler selector item for the default compilers for which we have support built-in.
 *
 * @author Sten Wessel
 */
class BuiltinCompilerItem(val compiler: SupportedLatexCompiler) : LatexCompilerComboBoxItem {

    override val presentableText
        get() = compiler.displayName

    override val command
        get() = compiler.executableName

    override val order = 2
}
