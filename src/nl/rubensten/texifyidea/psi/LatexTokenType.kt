package nl.rubensten.texifyidea.psi

import com.intellij.psi.tree.IElementType
import nl.rubensten.texifyidea.LatexLanguage
import org.jetbrains.annotations.NonNls

/**
 * @author Sten Wessel
 */
class LatexTokenType(@NonNls debugName: String) : IElementType(debugName, LatexLanguage.INSTANCE) {

    override fun toString() = "LatexTokenType." + super.toString()
}