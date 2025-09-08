package nl.hannahsten.texifyidea.structure.latex

import com.intellij.navigation.ItemPresentation
import nl.hannahsten.texifyidea.TexifyIcons
import nl.hannahsten.texifyidea.psi.LatexCommands
import nl.hannahsten.texifyidea.reference.InputFileReference

/**
 * @author Hannah Schellekens
 */
class LatexIncludePresentation(labelCommand: LatexCommands) : ItemPresentation {

    private val fileName by lazy { labelCommand.references.filterIsInstance<InputFileReference>().joinToString { it.refText } }

    // This method has to be really quick because it is used for example in sorting entries before they are shown to the user in the structure popup, so we cannot resolve to the actual file here
    override fun getPresentableText() = fileName

    override fun getLocationString() = ""

    override fun getIcon(b: Boolean) = TexifyIcons.DOT_INCLUDE
}
