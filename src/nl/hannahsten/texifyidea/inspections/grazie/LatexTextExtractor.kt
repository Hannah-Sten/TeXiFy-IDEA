package nl.hannahsten.texifyidea.inspections.grazie

import com.intellij.grazie.text.TextContent
import com.intellij.grazie.text.TextExtractor
import com.intellij.psi.PsiComment
import com.intellij.psi.PsiElement
import com.intellij.psi.util.elementType
import nl.hannahsten.texifyidea.psi.LatexCommands
import nl.hannahsten.texifyidea.psi.LatexContent
import nl.hannahsten.texifyidea.psi.LatexNormalText
import nl.hannahsten.texifyidea.psi.LatexParameter
import nl.hannahsten.texifyidea.psi.LatexTypes.*

/**
 * Explains to Grazie which psi elements contain text and which don't.
 */
class LatexTextExtractor : TextExtractor() {

    override fun buildTextContent(root: PsiElement, allowedDomains: MutableSet<TextContent.TextDomain>): TextContent? {
        val domain = when (root) {
            is PsiComment -> TextContent.TextDomain.COMMENTS
            is LatexNormalText -> TextContent.TextDomain.PLAIN_TEXT
            is LatexCommands -> TextContent.TextDomain.LITERALS // previously NON_TEXT
            is LatexParameter -> TextContent.TextDomain.LITERALS
            // It is important to return null instead of TextContent, otherwise Grazie will not search higher up in the psi tree, and we will not get this function called for non-leaf elements and the above lines will not work
            else -> return null
        }

        return TextContent.builder().build(root, domain)
    }
}