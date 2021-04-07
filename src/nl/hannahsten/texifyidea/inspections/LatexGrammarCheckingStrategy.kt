package nl.hannahsten.texifyidea.inspections

import com.intellij.grazie.grammar.strategy.GrammarCheckingStrategy
import com.intellij.grazie.grammar.strategy.StrategyUtils
import com.intellij.grazie.grammar.strategy.impl.RuleGroup
import com.intellij.grazie.utils.LinkedSet
import com.intellij.grazie.utils.parents
import com.intellij.grazie.utils.toLinkedSet
import com.intellij.psi.PsiComment
import com.intellij.psi.PsiElement
import com.intellij.refactoring.suggested.endOffset
import com.intellij.refactoring.suggested.startOffset
import nl.hannahsten.texifyidea.psi.*
import nl.hannahsten.texifyidea.util.childrenOfType
import nl.hannahsten.texifyidea.util.merge
import nl.hannahsten.texifyidea.util.overlaps

class LatexGrammarCheckingStrategy : GrammarCheckingStrategy {

    private fun PsiElement.isNotInMathEnvironment() = parents().none { it is LatexMathEnvironment }

    private fun PsiElement.isNotInSquareBrackets() = parents().find { it is LatexGroup || it is LatexOptionalParam }
        ?.let { it is LatexGroup } ?: true

    // Note that adding a root which could appear in another root could mess up the ranges below
    // because then 'root'.text != text.toString() anymore
    override fun isMyContextRoot(element: PsiElement) =
        element is LatexContent && element.isNotInMathEnvironment() && element.isNotInSquareBrackets()

    override fun getStealthyRanges(root: PsiElement, text: CharSequence): LinkedSet<IntRange> {
        // Only keep normaltext, assuming other things (like inline math) need to be ignored.
        val ranges = root.childrenOfType(LatexNormalText::class)
            .asSequence()
            .filter { it.isNotInMathEnvironment() && it.isNotInSquareBrackets() }
            // Ranges that we need to keep
            // Note that textRangeInParent will not be correct because that's the text range in the direct parent, not in the root
            // Also note that ranges have to be valid in 'text' and not in 'root'
            .flatMap { listOf(it.textRange.startOffset - root.startOffset, it.textRange.endOffset - root.startOffset) }
            .sorted()
            .toMutableList()
            // Make sure that if the root does not start/end with normal text, that those parts are excluded
            .also { it.add(0, 0) }
            .also { it.add(root.endOffset) }
            // To get the ranges that we need to ignore
            .chunked(2) { IntRange(it[0], it[1]) }
            .filter { it.first < it.last && it.first >= 0 && it.last <= text.length }
            .toMutableSet()

        // There is still a bit of a problem, because when stitching together the NormalTexts, whitespace is lost
        // so this leads Grazie to think that there is no space there, while in fact there may or may not be

        // Currently, GrammarChecker does not handle overlapped ranges, so we do that ourselves
        for (range in StrategyUtils.indentIndexes(text, setOf(' '))) {
            val overlapped = ranges.filter { range.overlaps(it) }
            ranges.removeAll(overlapped)
            ranges.add(range.merge(overlapped))
        }
        return ranges.toLinkedSet()
    }

    override fun isEnabledByDefault() = true

    override fun getContextRootTextDomain(root: PsiElement): GrammarCheckingStrategy.TextDomain {
        return when (root) {
            is PsiComment -> GrammarCheckingStrategy.TextDomain.COMMENTS
            is LatexContent -> GrammarCheckingStrategy.TextDomain.PLAIN_TEXT
            is LatexCommands -> GrammarCheckingStrategy.TextDomain.NON_TEXT
            is LatexParameter -> GrammarCheckingStrategy.TextDomain.LITERALS
            else -> GrammarCheckingStrategy.TextDomain.NON_TEXT
        }
    }

    override fun getIgnoredRuleGroup(root: PsiElement, child: PsiElement): RuleGroup {
        return RuleGroup.PUNCTUATION
    }
}
