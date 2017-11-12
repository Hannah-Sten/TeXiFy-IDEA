package nl.rubensten.texifyidea.psi

import com.intellij.psi.tree.IElementType
import nl.rubensten.texifyidea.BibtexLanguage

/**
 * @author Ruben Schellekens
 */
open class BibtexTokenType(debugName: String) : IElementType(debugName, BibtexLanguage) {

    override fun toString() = "BibtexTokenType." + super.toString()
}