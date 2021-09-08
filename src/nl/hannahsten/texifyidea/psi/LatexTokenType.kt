package nl.hannahsten.texifyidea.psi

import com.intellij.psi.tree.IElementType
import nl.hannahsten.texifyidea.LatexLanguage
import org.jetbrains.annotations.NonNls

/**
 * @author Sten Wessel
 */
class LatexTokenType(@NonNls debugName: String) : IElementType(debugName, LatexLanguage) {

    override fun toString() = "LatexTokenType." + super.toString()
}