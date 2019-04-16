package nl.rubensten.texifyidea.structure.latex

import com.intellij.navigation.ItemPresentation
import nl.rubensten.texifyidea.TexifyIcons
import nl.rubensten.texifyidea.psi.LatexCommands

/**
 * @author Ruben Schellekens
 */
class LatexNewCommandPresentation(newCommand: LatexCommands) : ItemPresentation {

    private val newCommandName: String
    private val locationString: String

    init {
        // Fetch parameter amount.
        val optional = newCommand.optionalParameters
        var params = -1
        if (!optional.isEmpty()) {
            try {
                params = Integer.parseInt(optional[0])
            }
            catch (ignored: NumberFormatException) {
            }
        }
        val suffix = if (params != -1) "{x$params}" else ""

        // Get command name.
        val required = newCommand.requiredParameters
        this.newCommandName = required.firstOrNull() ?: "" + suffix

        // Get value.
        locationString = if (required.size > 1) {
            required[1]
        }
        else ""
    }

    override fun getPresentableText() = newCommandName

    override fun getLocationString() = locationString

    override fun getIcon(b: Boolean) = TexifyIcons.DOT_COMMAND!!
}