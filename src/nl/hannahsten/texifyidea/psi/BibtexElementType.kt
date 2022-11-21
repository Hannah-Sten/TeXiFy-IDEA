package nl.hannahsten.texifyidea.psi

import com.intellij.psi.tree.IElementType
import nl.hannahsten.texifyidea.grammar.BibtexLanguage

/**
 * @author Hannah Schellekens
 */
open class BibtexElementType(debugName: String) : IElementType(debugName, BibtexLanguage)