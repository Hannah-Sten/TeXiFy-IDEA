package nl.rubensten.texifyidea.psi

import com.intellij.psi.tree.IElementType
import nl.rubensten.texifyidea.BibtexLanguage

/**
 * @author Ruben Schellekens
 */
open class BibtexElementType(debugName: String) : IElementType(debugName, BibtexLanguage)