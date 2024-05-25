package nl.hannahsten.texifyidea.structure.latex

import com.intellij.navigation.ItemPresentation
import nl.hannahsten.texifyidea.psi.LatexCommands
import nl.hannahsten.texifyidea.util.files.document
import nl.hannahsten.texifyidea.util.parser.lineNumber
import nl.hannahsten.texifyidea.util.parser.nextCommand
import javax.swing.Icon

/**
 * @author Hannah Schellekens
 */
class LatexOtherCommandPresentation(val command: LatexCommands, private val icon: Icon) : ItemPresentation {

    override fun getPresentableText() = command.name

    override fun getLocationString() = command.lineNumber().toString()

    override fun getIcon(b: Boolean) = icon
}