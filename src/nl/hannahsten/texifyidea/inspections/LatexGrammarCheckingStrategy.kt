// package nl.hannahsten.texifyidea.inspections
//
// import com.intellij.grazie.grammar.strategy.GrammarCheckingStrategy
// import com.intellij.grazie.grammar.strategy.StrategyUtils
// import com.intellij.grazie.utils.parents
// import com.intellij.psi.PsiComment
// import com.intellij.psi.PsiElement
// import nl.hannahsten.texifyidea.psi.*
//
// class LatexGrammarCheckingStrategy : GrammarCheckingStrategy {
//     private fun PsiElement.isNotInMathEnvironment() = parents().none { it is LatexMathEnvironment }
//
//     private fun PsiElement.isNotInSquareBrackets() = parents().find { it is LatexGroup || it is LatexOptionalParam }
//             ?.let { it is LatexGroup } ?: true
//
//     override fun isMyContextRoot(element: PsiElement) = element is LatexNormalText && element.isNotInMathEnvironment() && element.isNotInSquareBrackets() || element is PsiComment
//
//     override fun getStealthyRanges(root: PsiElement, text: CharSequence) = StrategyUtils.indentIndexes(text, setOf(' '))
//
//     override fun isEnabledByDefault() = true
//
//     override fun getContextRootTextDomain(root: PsiElement): GrammarCheckingStrategy.TextDomain {
//         return when (root) {
//             is PsiComment -> GrammarCheckingStrategy.TextDomain.COMMENTS
//             is LatexNormalText -> GrammarCheckingStrategy.TextDomain.PLAIN_TEXT
//             is LatexCommands -> GrammarCheckingStrategy.TextDomain.NON_TEXT
//             is LatexParameter -> GrammarCheckingStrategy.TextDomain.LITERALS
//             else -> GrammarCheckingStrategy.TextDomain.NON_TEXT
//         }
//     }
// }
