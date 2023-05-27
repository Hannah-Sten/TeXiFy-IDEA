package nl.hannahsten.texifyidea.structure.latex

import com.intellij.navigation.ItemPresentation
import nl.hannahsten.texifyidea.psi.LatexCommands
import nl.hannahsten.texifyidea.util.parser.nextCommand
import javax.swing.Icon

/**
 * @author Hannah Schellekens
 */
class LatexOtherCommandPresentation(command: LatexCommands, private val icon: Icon) : ItemPresentation {

    private val commandName = command.name
    private var locationString: String

    init {
        val firstNext = command.nextCommand()
        if (firstNext != null) {
            val lookup = firstNext.commandToken.text
            this.locationString = lookup ?: ""
        }

        locationString = ""
    }

    override fun getPresentableText() = commandName

    override fun getLocationString() = locationString

    override fun getIcon(b: Boolean) = icon
}