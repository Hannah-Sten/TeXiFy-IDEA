package nl.hannahsten.texifyidea.run.ui.compiler

import nl.hannahsten.texifyidea.run.compiler.SupportedCompiler
import nl.hannahsten.texifyidea.run.executable.Executable
import nl.hannahsten.texifyidea.run.executable.SupportedExecutable
import nl.hannahsten.texifyidea.run.step.CompileStep

/**
 * executable selector item for the default executables for which we have support built-in.
 *
 * @author Sten Wessel
 */
class BuiltinExecutableItem(val executable: SupportedExecutable) : ExecutableComboBoxItem {

    override val presentableText
        get() = executable.displayName

    override val command
        get() = executable.executableName

    override val order = 2
}
