package nl.hannahsten.texifyidea.psi

import com.intellij.psi.tree.IElementType
import nl.hannahsten.texifyidea.grammar.LatexLanguage
import org.jetbrains.annotations.NonNls

/**
 * @author Sten Wessel
 */
class LatexElementType(@NonNls debugName: String) : IElementType(debugName, LatexLanguage)