package nl.hannahsten.texifyidea.inspections

import com.intellij.grazie.grammar.strategy.GrammarCheckingStrategy
import com.intellij.grazie.grammar.strategy.StrategyUtils
import com.intellij.grazie.grammar.strategy.impl.RuleGroup
import com.intellij.grazie.utils.LinkedSet
import com.intellij.grazie.utils.parents
import com.intellij.grazie.utils.toLinkedSet
import com.intellij.psi.PsiComment
import com.intellij.psi.PsiElement
import nl.hannahsten.texifyidea.psi.*
import nl.hannahsten.texifyidea.util.childrenOfType

class LatexGrammarCheckingStrategy : GrammarCheckingStrategy {

    private fun PsiElement.isNotInMathEnvironment() = parents().none { it is LatexMathEnvironment }

    private fun PsiElement.isNotInSquareBrackets() = parents().find { it is LatexGroup || it is LatexOptionalParam }
        ?.let { it is LatexGroup } ?: true

    override fun isMyContextRoot(element: PsiElement) =
        element is LatexContent && element.isNotInMathEnvironment() && element.isNotInSquareBrackets() || element is PsiComment

    override fun getStealthyRanges(root: PsiElement, text: CharSequence): LinkedSet<IntRange> {
        // Only keep normaltext, assuming other things (like inline math) need to be ignored.
        val ranges = root.childrenOfType(LatexNormalText::class)
            // Ranges that we need to keep
            .flatMap { listOf(it.textRangeInParent.startOffset, it.textRangeInParent.endOffset) }
            .sorted()
            // Shift by 1
            .drop(1).dropLast(1)
            // To get the ranges that we need to ignore
            .zipWithNext { a: Int, b: Int -> IntRange(a, b) }
            .toLinkedSet()
        ranges.addAll(StrategyUtils.indentIndexes(text, setOf(' ')))
        return ranges
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
