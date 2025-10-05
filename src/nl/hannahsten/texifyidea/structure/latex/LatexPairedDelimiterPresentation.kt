package nl.hannahsten.texifyidea.structure.latex

import com.intellij.navigation.ItemPresentation
import nl.hannahsten.texifyidea.TexifyIcons
import nl.hannahsten.texifyidea.psi.LatexCommands

class LatexPairedDelimiterPresentation(newCommand: LatexCommands) : ItemPresentation {

    private val newCommandName: String
    private val locationString: String

    init {
        // Get command name.
        val required = newCommand.requiredParametersText()
        newCommandName = if (required.isNotEmpty()) {
            required.first()
        }
        else ""

        locationString = if (required.size >= 3) {
            when (newCommand.name) {
                "\\DeclarePairedDelimiterXPP" -> (1..4).joinToString(" ") { required[it] }
                else -> "${required[1]} ${required[2]}"
            }
        }
        else ""
    }

    override fun getPresentableText() = newCommandName

    override fun getLocationString() = locationString

    override fun getIcon(b: Boolean) = TexifyIcons.DOT_COMMAND
}