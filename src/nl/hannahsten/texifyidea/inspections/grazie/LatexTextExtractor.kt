package nl.hannahsten.texifyidea.inspections.grazie

import com.intellij.grazie.text.TextContent
import com.intellij.grazie.text.TextExtractor
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiComment
import com.intellij.psi.PsiElement
import com.intellij.psi.util.elementType
import com.intellij.refactoring.suggested.startOffset
import nl.hannahsten.texifyidea.psi.LatexCommands
import nl.hannahsten.texifyidea.psi.LatexContent
import nl.hannahsten.texifyidea.psi.LatexNormalText
import nl.hannahsten.texifyidea.psi.LatexParameter
import nl.hannahsten.texifyidea.psi.LatexTypes.*
import nl.hannahsten.texifyidea.util.childrenOfType

/**
 * Explains to Grazie which psi elements contain text and which don't.
 */
class LatexTextExtractor : TextExtractor() {

    override fun buildTextContent(root: PsiElement, allowedDomains: MutableSet<TextContent.TextDomain>): TextContent? {

        val domain = when (root) {
            is LatexNormalText -> TextContent.TextDomain.PLAIN_TEXT
            is LatexCommands -> TextContent.TextDomain.LITERALS // previously NON_TEXT
            is LatexParameter -> TextContent.TextDomain.LITERALS
            // It is important to return null instead of TextContent, otherwise Grazie will not search higher up in the psi tree, and we will not get this function called for non-leaf elements and the above lines will not work
            else -> return null
        }

        // Ignore comments manually, because for some reason they are not processed as separate roots
        val textContent = TextContent.builder().build(root, domain)
        val ignored = root
            .childrenOfType<PsiComment>()
            // Get text range relative to root
            .map { TextRange(it.textRange.startOffset - root.startOffset - 1,
                it.textRange.endOffset - root.startOffset + 1) }
            .map { TextContent.Exclusion.exclude(it) }

        return textContent?.excludeRanges(ignored)
    }
}