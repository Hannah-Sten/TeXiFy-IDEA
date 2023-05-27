package nl.hannahsten.texifyidea.inspections.grazie

import com.intellij.grazie.grammar.strategy.StrategyUtils
import com.intellij.grazie.text.TextContent
import com.intellij.grazie.text.TextExtractor
import com.intellij.psi.PsiElement
import com.intellij.refactoring.suggested.startOffset
import nl.hannahsten.texifyidea.psi.*
import nl.hannahsten.texifyidea.util.*
import nl.hannahsten.texifyidea.util.parser.childrenOfType
import nl.hannahsten.texifyidea.util.parser.endOffset
import nl.hannahsten.texifyidea.util.parser.parents

/**
 * Explains to Grazie which psi elements contain text and which don't.
 */
class LatexTextExtractor : TextExtractor() {

    override fun buildTextContent(root: PsiElement, allowedDomains: MutableSet<TextContent.TextDomain>): TextContent? {
        if (root !is LatexContent) {
            // It is important to return null instead of TextContent, otherwise Grazie will not search higher up in the psi tree, and we will not get this function called for non-leaf elements and the above lines will not work
            return null
        }

        // Since Grazie works by first checking leaf elements, and if it gets null tries one level higher, we cannot return anything (e.g. literal for a command, comment for comments) other than LatexContent because then LatexContent itself will not be used as a root.
        // However, we do need it as a root because we need to filter out certain things like inline math ourselves, so that we can make sure all the whitespace around ignored items is correct.
        val domain = TextContent.TextDomain.PLAIN_TEXT

        val textContent = TextContent.builder().build(root, domain) ?: return null
        val stealthyRanges = getStealthyRanges(root)
            // Convert IntRange (inclusive end) to TextRange (exclusive end)
            .map { TextContent.Exclusion.exclude(it.toTextRange()) }
            .filter { it.start >= 0 && it.end <= textContent.length }

        return textContent.excludeRanges(stealthyRanges)
    }

    /**
     * Get ranges to ignore.
     * Note: IntRange has an inclusive end.
     */
    private fun getStealthyRanges(root: PsiElement): List<IntRange> {
        // Getting text takes time, so we only do it once
        val rootText = root.text

        // Only keep normaltext, assuming other things (like inline math) need to be ignored.
        val ranges = root.childrenOfType(LatexNormalText::class)
            .asSequence()
            .filter { it.isNotInMathEnvironment() && it.isNotInSquareBrackets() }
            // Ranges that we need to keep
            // Note that textRangeInParent will not be correct because that's the text range in the direct parent, not in the root
            .flatMap {
                // I have no idea what happens here. I don't think Grazie uses the same indices and text as root.text, because it doesn't behave consistently when I move around indices, so it may appear we are ignoring too much or too little while in practice the inspections may work.
                var start = it.textRange.startOffset - root.startOffset
                if (start > 0 && rootText[start - 1] != '\n' && rootText[start - 1] != ' ') {
                    // Support sentence ends with inline math
                    start -= 1
                }
                listOf(
                    start,
                    // -1 Because endOffset is exclusive but we are working with inclusive end here
                    it.textRange.endOffset - 1 - root.startOffset
                )
            }
            .sorted()
            .toMutableList()
            // Make sure that if the root does not start/end with normal text, that those parts are excluded
            .also { it.add(0, 0) }
            .also { it.add(root.endOffset()) }
            // To get the ranges that we need to ignore
            // -1 because IntRange has inclusive end, but we want to exclude all letters _excluding_ the letter where the normal text started
            .chunked(2) { IntRange(it[0], it[1] - 1) }
            .filter { it.first < it.last && it.first >= 0 && it.last < rootText.length }
            .toMutableSet()

        // There is still a bit of a problem, because when stitching together the NormalTexts, whitespace is lost
        // so this leads Grazie to think that there is no space there, while in fact there may or may not be

        // Currently, GrammarChecker does not handle overlapped ranges, so we do that ourselves
        for (range in StrategyUtils.indentIndexes(root.text, setOf(' '))) {
            val overlapped = ranges.filter { range.overlaps(it) }
            ranges.removeAll(overlapped.toSet())
            ranges.add(range.merge(overlapped))
        }
        return ranges.sortedBy { it.first }
    }

    private fun PsiElement.isNotInMathEnvironment() = parents().none { it is LatexMathEnvironment }

    private fun PsiElement.isNotInSquareBrackets() = parents().find { it is LatexGroup || it is LatexOptionalParam }
        ?.let { it is LatexGroup } ?: true
}