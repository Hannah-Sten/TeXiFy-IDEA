package nl.hannahsten.texifyidea.inspections.grazie

import com.intellij.grazie.text.TextContent
import com.intellij.grazie.text.TextExtractor
import com.intellij.psi.PsiComment
import com.intellij.psi.PsiElement
import nl.hannahsten.texifyidea.psi.LatexCommands
import nl.hannahsten.texifyidea.psi.LatexContent
import nl.hannahsten.texifyidea.psi.LatexParameter

/**
 * Explains to Grazie which psi elements contain text and which don't.
 */
class LatexTextExtractor : TextExtractor() {

    override fun buildTextContent(element: PsiElement, allowedDomains: MutableSet<TextContent.TextDomain>): TextContent? {
        val domain = when (element) {
            is PsiComment -> TextContent.TextDomain.COMMENTS
            is LatexContent -> TextContent.TextDomain.PLAIN_TEXT
            is LatexCommands -> TextContent.TextDomain.LITERALS // previously NON_TEXT
            is LatexParameter -> TextContent.TextDomain.LITERALS
            else -> TextContent.TextDomain.LITERALS // previously NON_TEXT
        }

        if (domain !in allowedDomains) return null

        return TextContent.builder().build(element, domain)
    }
}