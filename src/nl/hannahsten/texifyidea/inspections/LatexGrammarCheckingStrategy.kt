package nl.hannahsten.texifyidea.inspections

import com.intellij.grazie.grammar.strategy.GrammarCheckingStrategy
import com.intellij.grazie.grammar.strategy.StrategyUtils
import com.intellij.grazie.grammar.strategy.impl.ReplaceCharRule
import com.intellij.grazie.utils.*
import com.intellij.psi.PsiElement
import nl.hannahsten.texifyidea.psi.*

class LatexGrammarCheckingStrategy : GrammarCheckingStrategy {
    private fun PsiElement.isNotInMathEnvironment() = parents().none { it is LatexMathEnvironment }

    private fun PsiElement.isNotInSquareBrackets() = parents().find { it is LatexGroup || it is LatexOpenGroup }
            ?.let { it is LatexGroup } ?: true

    override fun isMyContextRoot(element: PsiElement) = element is LatexNormalText && element.isNotInMathEnvironment() && element.isNotInSquareBrackets()

    override fun isTypoAccepted(root: PsiElement, typoRange: IntRange, ruleRange: IntRange) = !typoRange.isAtStart(root) && !typoRange.isAtEnd(root)

    override fun getReplaceCharRules(root: PsiElement) = emptyList<ReplaceCharRule>()

    override fun getStealthyRanges(root: PsiElement, text: CharSequence) = StrategyUtils.indentIndexes(text, setOf(' '))
}
