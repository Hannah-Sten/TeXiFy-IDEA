package nl.hannahsten.texifyidea.insight

import com.intellij.openapi.editor.ElementColorProvider
import com.intellij.psi.PsiElement
import com.intellij.psi.impl.source.tree.LeafPsiElement
import nl.hannahsten.texifyidea.psi.LatexCommands
import nl.hannahsten.texifyidea.util.Magic
import nl.hannahsten.texifyidea.util.firstParentOfType
import nl.hannahsten.texifyidea.util.getRequiredArgumentValueByName
import nl.hannahsten.texifyidea.util.usesColor
import java.awt.Color

class LatexElementColorProvider : ElementColorProvider {
    override fun setColorTo(element: PsiElement, color: Color) {}

    override fun getColorFrom(element: PsiElement): Color? {
        if (element is LeafPsiElement) {
            val command = element.firstParentOfType(LatexCommands::class)
            if (command.usesColor()) {
                // Find the values of the arguments where we expect colors, and check
                // if the text of this element appears in one of these places.
                // NOTE: this avoids that colors in an argument with just text
                // (e.g. in \textcolor) appear in the gutter, but colors that
                // are mentioned in this text that ALSO appear in a `color`
                // argument of the same command do show up in the gutter, resulting
                // in this color showing up twice.
                val colorArguments = listOf("name", "color").map {
                    command?.getRequiredArgumentValueByName(it)
                }
                if (colorArguments.any { it?.contains(element.text) == true }) {
                    return Color(Magic.Colors.defaultXcolors[element.text] ?: return null)
                }
            }
        }
        return null
    }
}