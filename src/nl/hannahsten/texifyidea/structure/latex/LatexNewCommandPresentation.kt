package nl.hannahsten.texifyidea.structure.latex

import com.intellij.navigation.ItemPresentation
import nl.hannahsten.texifyidea.TexifyIcons
import nl.hannahsten.texifyidea.lang.commands.LatexNewDefinitionCommand
import nl.hannahsten.texifyidea.lang.commands.LatexXparseCommand
import nl.hannahsten.texifyidea.psi.LatexCommands
import nl.hannahsten.texifyidea.util.psi.toStringMap
import nl.hannahsten.texifyidea.util.psi.nextCommand

/**
 * @author Hannah Schellekens
 */
class LatexNewCommandPresentation(newCommand: LatexCommands) : ItemPresentation {

    private val newCommandName: String
    private val locationString: String

    init {
        // Fetch parameter amount.
        val optional = newCommand.getOptionalParameterMap().toStringMap().keys.toList()
        var params = -1
        if (optional.isNotEmpty()) {
            try {
                params = Integer.parseInt(optional[0])
            }
            catch (ignored: NumberFormatException) {
            }
        }
        val suffix = if (params != -1) "{x$params}" else ""

        // Get command name.
        val required = newCommand.getRequiredParameters()
        val command = if (required.isNotEmpty()) {
            required.first()
        }
        else {
            // If there are no required parameters, the user may have left out the braces around the first one, so we get it manually
            newCommand.nextCommand()?.commandToken?.text
        }

        this.newCommandName = command ?: ("" + suffix)

        // Get the definition to show in place of the location string.
        locationString = when {
            newCommand.commandToken.text == "\\" + LatexNewDefinitionCommand.NEWCOMMAND.command && required.size >= 2 -> required[1]
            newCommand.commandToken.text == "\\" + LatexXparseCommand.NEWDOCUMENTCOMMAND.command && required.size >= 3 -> required[2]
            else -> ""
        }
    }

    override fun getPresentableText() = newCommandName

    override fun getLocationString() = locationString

    override fun getIcon(b: Boolean) = TexifyIcons.DOT_COMMAND
}