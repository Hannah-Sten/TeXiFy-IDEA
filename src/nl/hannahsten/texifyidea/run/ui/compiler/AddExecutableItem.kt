package nl.hannahsten.texifyidea.run.ui.compiler

import nl.hannahsten.texifyidea.run.executable.Executable

/**
 * LaTeX compiler selector item which allows the user to select a custom executable.
 *
 * @author Sten Wessel
 */
class AddExecutableItem(type: String) : ExecutableComboBoxItem {

    override val presentableText = "Select alternative $type..."
    override val command = "latex"
    override val order = Int.MAX_VALUE
}
