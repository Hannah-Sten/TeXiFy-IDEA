package nl.hannahsten.texifyidea.insight

import com.intellij.openapi.editor.ElementColorProvider
import com.intellij.psi.PsiElement
import com.intellij.psi.impl.source.tree.LeafPsiElement
import nl.hannahsten.texifyidea.psi.LatexCommands
import nl.hannahsten.texifyidea.util.Magic
import nl.hannahsten.texifyidea.util.firstParentOfType
import nl.hannahsten.texifyidea.util.usesColor
import java.awt.Color

class LatexElementColorProvider : ElementColorProvider {
    override fun setColorTo(element: PsiElement, color: Color) {}

    override fun getColorFrom(element: PsiElement): Color? {
        if (element is LeafPsiElement) {
            val command = element.firstParentOfType(LatexCommands::class)
            if (command.usesColor()) {
                if (element.text in Magic.Colors.defaultXcolors) return Color.RED
            }
        }
        return null
    }
}