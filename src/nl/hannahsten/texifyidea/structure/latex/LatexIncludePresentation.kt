package nl.hannahsten.texifyidea.structure.latex

import com.intellij.navigation.ItemPresentation
import nl.hannahsten.texifyidea.TexifyIcons
import nl.hannahsten.texifyidea.psi.LatexCommands
import nl.hannahsten.texifyidea.util.parser.getIncludedFiles
import nl.hannahsten.texifyidea.util.updateAndGetIncludeCommands

/**
 * @author Hannah Schellekens
 */
class LatexIncludePresentation(labelCommand: LatexCommands) : ItemPresentation {

    private val fileName: String

    init {
        if (labelCommand.name !in updateAndGetIncludeCommands(labelCommand.project)) {
            throw IllegalArgumentException("Command $labelCommand is no include command")
        }

        this.fileName = labelCommand.getIncludedFiles(true).joinToString { it.name }
    }

    override fun getPresentableText() = fileName

    override fun getLocationString() = ""

    override fun getIcon(b: Boolean) = TexifyIcons.DOT_INCLUDE
}
