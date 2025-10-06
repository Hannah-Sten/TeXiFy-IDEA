package nl.hannahsten.texifyidea.structure.latex

import com.intellij.navigation.ItemPresentation
import nl.hannahsten.texifyidea.TexifyIcons

/**
 * @author Hannah Schellekens
 */
class LatexLabelPresentation(
    private val locationString: String,
    private val presentableText: String
) : ItemPresentation {

    override fun getPresentableText() = presentableText

    override fun getLocationString() = locationString

    override fun getIcon(b: Boolean) = TexifyIcons.DOT_LABEL
}
