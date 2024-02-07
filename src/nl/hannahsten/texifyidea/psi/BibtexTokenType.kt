package nl.hannahsten.texifyidea.psi

import com.intellij.psi.tree.IElementType
import nl.hannahsten.texifyidea.grammar.BibtexLanguage

/**
 * @author Hannah Schellekens
 */
open class BibtexTokenType(debugName: String) : IElementType(debugName, BibtexLanguage) {

    override fun toString() = "BibtexTokenType." + super.toString()
}