package nl.hannahsten.texifyidea.run.ui.compiler

import nl.hannahsten.texifyidea.run.executable.SupportedExecutable

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
