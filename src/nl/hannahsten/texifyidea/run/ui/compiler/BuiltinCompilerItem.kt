package nl.hannahsten.texifyidea.run.ui.compiler

import nl.hannahsten.texifyidea.run.compiler.SupportedCompiler
import nl.hannahsten.texifyidea.run.step.CompileStep

/**
 * LaTeX compiler selector item for the default compilers for which we have support built-in.
 *
 * @author Sten Wessel
 */
class BuiltinCompilerItem<in S : CompileStep>(val compiler: SupportedCompiler<S>) : CompilerComboBoxItem {

    override val presentableText
        get() = compiler.displayName

    override val command
        get() = compiler.executableName

    override val order = 2
}
