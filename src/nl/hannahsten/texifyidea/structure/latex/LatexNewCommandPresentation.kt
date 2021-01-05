package nl.hannahsten.texifyidea.structure.latex

import com.intellij.navigation.ItemPresentation
import nl.hannahsten.texifyidea.TexifyIcons
import nl.hannahsten.texifyidea.psi.LatexCommands
import nl.hannahsten.texifyidea.psi.toStringMap
import nl.hannahsten.texifyidea.util.nextCommand

/**
 * @author Hannah Schellekens
 */
class LatexNewCommandPresentation(newCommand: LatexCommands) : ItemPresentation {

    private val newCommandName: String
    private val locationString: String

    init {
        // Fetch parameter amount.
        val optional = newCommand.optionalParameterMap.toStringMap().keys.toList()
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
        val required = newCommand.requiredParameters
        val command = if (required.size > 0) {
            required.first()
        }
        else {
            // If there are no required parameters, the user may have left out the braces around the first one, so we get it manually
            newCommand.nextCommand()?.commandToken?.text
        }

        this.newCommandName = command ?: "" + suffix

        // Get value.
        locationString = if (required.size > 1) {
            when (newCommand.commandToken.text) {
                "\\newcommand" -> required[1]
                "\\NewDocumentCommand" -> required[2]
                else -> ""
            }
        }
        else ""
    }

    override fun getPresentableText() = newCommandName

    override fun getLocationString() = locationString

    override fun getIcon(b: Boolean) = TexifyIcons.DOT_COMMAND
}