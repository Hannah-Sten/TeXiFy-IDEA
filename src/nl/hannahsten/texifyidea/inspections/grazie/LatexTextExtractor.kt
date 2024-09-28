package nl.hannahsten.texifyidea.inspections.grazie

import com.intellij.grazie.grammar.strategy.StrategyUtils
import com.intellij.grazie.text.TextContent
import com.intellij.grazie.text.TextExtractor
import com.intellij.psi.PsiElement
import com.intellij.psi.util.startOffset
import nl.hannahsten.texifyidea.lang.commands.Argument
import nl.hannahsten.texifyidea.lang.commands.LatexCommand
import nl.hannahsten.texifyidea.psi.*
import nl.hannahsten.texifyidea.util.merge
import nl.hannahsten.texifyidea.util.overlaps
import nl.hannahsten.texifyidea.util.parser.childrenOfType
import nl.hannahsten.texifyidea.util.parser.endOffset
import nl.hannahsten.texifyidea.util.parser.firstParentOfType
import nl.hannahsten.texifyidea.util.parser.parents
import nl.hannahsten.texifyidea.util.toTextRange

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
        val ranges = (root.childrenOfType(LatexNormalText::class) + root.childrenOfType<LatexParameterText>())
            .asSequence()
            .filter { it.isNotInMathEnvironment() && it.isNotInSquareBrackets() }
            // Ranges that we need to keep
            // Note that textRangeInParent will not be correct because that's the text range in the direct parent, not in the root
            .flatMap { text ->
                // Skip arguments of non-text commands
                if (text is LatexParameterText && LatexCommand.lookup(text.firstParentOfType(LatexCommands::class)?.name)?.firstOrNull()?.arguments?.any { it.type == Argument.Type.TEXT } != true) {
                    return@flatMap emptyList()
                }

                var start = text.textRange.startOffset - root.startOffset
                // If LatexNormalText starts after a newline following a command, the newline is not part of the LatexNormalText so we include it manually to make sure that it is seen as a space between sentences
                // NOTE: it is not allowed to start the text we send to Grazie with a newline! If we do, then Grazie will just not do anything. So we exclude the newline for the first normal text in the file.
                if (setOf(' ', '\n').contains(rootText.getOrNull(start - 1)) && root.childrenOfType(LatexNormalText::class).firstOrNull() != text
                ) {
                    //  We have to skip over indents to find the newline though (indents will be ignored later)
                    start -= rootText.substring(0, start).takeLastWhile { it.isWhitespace() }.length
                }

                // -1 Because endOffset is exclusive, but we are working with inclusive end here
                var end = text.textRange.endOffset - 1 - root.startOffset
                // If LatexNormalText ends, for example because it is followed by a command, we do want to include the space in front of the command, since it is still typeset as a space, which is not true for the space after the command
                // except when the space is followed by inline math, since we ignore inline math altogether (which is probably not correct) we should also ignore the space
                if (setOf(' ', '\n').contains(rootText.getOrNull(end + 1)) && rootText.getOrNull(end + 2) != '$') {
                    end += 1
                }
                listOf(start, end)
            }
            .sorted()
            .toMutableList()
            // Make sure that if the root does not start/end with normal text, that those parts are excluded
            .also { it.add(0, -1) }
            .also { it.add(root.endOffset()) }
            // To get the ranges that we need to ignore
            // + 1 because we want to exclude all letters _after_ the last letter that we want to keep (the ranges defined above are inclusive)
            // -1 because IntRange has inclusive end, but we want to exclude all letters _excluding_ the letter where the normal text started
            .chunked(2) { IntRange(it[0] + 1, it[1] - 1) }
            .filter { it.first <= it.last && it.first >= 0 && it.last < rootText.length }
            .toMutableSet()

        // There is still a bit of a problem, because when stitching together the NormalTexts, whitespace is lost
        // so this leads Grazie to think that there is no space there, while in fact there may or may not be

        // Add indents as ranges to ignore
        // Currently, GrammarChecker does not handle overlapped ranges, so we do that ourselves
        for (indent in StrategyUtils.indentIndexes(root.text, setOf(' '))) {
            val overlapped = ranges.filter { indent.overlaps(it) }
            ranges.removeAll(overlapped.toSet())
            ranges.add(indent.merge(overlapped))
        }
        // This is approximately (except at the start) the text we send to Grazie
//        val text = ranges.sortedBy { it.first }.flatMap { listOf(it.first, it.last) }.toMutableList().also { it.add(0, -1) }
//            .chunked(2) { if (it.size > 1) rootText.substring(it[0] + 1, it[1]) else null }
        return ranges.sortedBy { it.first }
    }

    private fun PsiElement.isNotInMathEnvironment() = parents().none { it is LatexMathEnvironment }

    private fun PsiElement.isNotInSquareBrackets() = parents().find { it is LatexGroup || it is LatexOptionalParam }
        ?.let { it is LatexGroup } ?: true
}