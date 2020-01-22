package nl.hannahsten.texifyidea.inspections

import com.intellij.grazie.grammar.strategy.GrammarCheckingStrategy
import com.intellij.grazie.grammar.strategy.StrategyUtils
import com.intellij.grazie.utils.parents
import com.intellij.psi.PsiElement
import nl.hannahsten.texifyidea.psi.LatexGroup
import nl.hannahsten.texifyidea.psi.LatexMathEnvironment
import nl.hannahsten.texifyidea.psi.LatexNormalText
import nl.hannahsten.texifyidea.psi.LatexOpenGroup

class LatexGrammarCheckingStrategy : GrammarCheckingStrategy {
    private fun PsiElement.isNotInMathEnvironment() = parents().none { it is LatexMathEnvironment }

    private fun PsiElement.isNotInSquareBrackets() = parents().find { it is LatexGroup || it is LatexOpenGroup }
            ?.let { it is LatexGroup } ?: true

    override fun isMyContextRoot(element: PsiElement) = element is LatexNormalText && element.isNotInMathEnvironment() && element.isNotInSquareBrackets()

    override fun getStealthyRanges(root: PsiElement, text: CharSequence) = StrategyUtils.indentIndexes(text, setOf(' '))
}
