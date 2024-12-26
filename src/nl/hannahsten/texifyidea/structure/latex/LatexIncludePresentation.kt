package nl.hannahsten.texifyidea.structure.latex

import com.intellij.navigation.ItemPresentation
import nl.hannahsten.texifyidea.TexifyIcons
import nl.hannahsten.texifyidea.psi.LatexCommands
import nl.hannahsten.texifyidea.util.parser.getIncludedFiles

/**
 * @author Hannah Schellekens
 */
class LatexIncludePresentation(labelCommand: LatexCommands) : ItemPresentation {

    private val fileName: String

    init {
        this.fileName = labelCommand.getIncludedFiles(true).joinToString { it.name }
    }

    override fun getPresentableText() = fileName

    override fun getLocationString() = ""

    override fun getIcon(b: Boolean) = TexifyIcons.DOT_INCLUDE
}
