package nl.hannahsten.texifyidea.refactoring

import com.intellij.psi.PsiElement
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import nl.hannahsten.texifyidea.file.LatexFileType
import nl.hannahsten.texifyidea.refactoring.myextractfunction.ExtractExpressionUi
import nl.hannahsten.texifyidea.refactoring.myextractfunction.withMockTargetExpressionChooser

class IntroduceVariableTest : BasePlatformTestCase() {
    fun testBasicCaret() = doTest(
        """
        My favorite number is 5.<caret>25
    """, listOf("5.25", "My favorite number is 5.25"), 0, """
        \newcommand{\mycommand}{5.25}My favorite number is \mycommand{}
    """
    )

    fun testBasicSelection() = doTest(
        """
        My favorite number is <selection>5.25</selection>
    """, emptyList(), 0, """
        \newcommand{\mycommand}{5.25}My favorite number is \mycommand{}
    """
    )

    fun testSentenceOffer() = doTest(
        """
        \documentclass[11pt]{article}

        \begin{document}
        
            \chapter{The significance of 2.718, or e}
        
            2.718 is a special number.
        
            ${'$'}\lim_{x \to \infty} (1+\frac{1}{x})^{x}=2.718${'$'}
        
            Some old guy discovered 2.7<caret>18 and was amazed!
        
            Not to be confused with 2.714, or  ${'$'}3\pi!-6\pi${'$'}
        
        \end{document}
    """, listOf("2.718", "Some old guy discovered 2.718 and was amazed!",
            """Some old guy discovered 2.7<carret>18 and was amazed!
        
            Not to be confused with 2.714, or  ${'$'}3\pi!-6\pi${'$'}"""
        ), 0, """
        \documentclass[11pt]{article}
        
        \newcommand{\mycommand}{2.718}

        \begin{document}
        
            \chapter{The significance of \mycommand{}, or e}
        
            \mycommand{} is a special number.
        
            ${'$'}\lim_{x \to \infty} (1+\frac{1}{x})^{x}=\mycommand{}${'$'}
        
            Some old guy discovered \mycommand{} and was amazed!
        
            Not to be confused with 2.714, or  ${'$'}3\pi!-6\pi${'$'}
        
        \end{document}
    """, true
    )

    fun testMultiTableSelection() = doTest(
        """
        \documentclass[11pt]{article}
        \begin{document}
            
            ${'$'}5.25 * 2.68291 = 450${'$'}
        
            \begin{table}[ht!]
                \begin{tabular}{| r |}
                    \hline
                    2.68291 \\
                    \hline
                \end{tabular}
            \end{table}
        
            \begin{table}[ht!]
                \begin{tabular}{| r |}
                    \hline
                    <selection>2.68291</selection>          \\
                    \hline
                \end{tabular}
            \end{table}
        
            Some may wonder why 2.68291 is so special.
        \end{document}
    """, emptyList(), 0, """
        \documentclass[11pt]{article}
        
        \newcommand{\mycommand}{}
        
        \begin{document}
            
            ${'$'}5.25 * \mycommand{} = 450${'$'}
        
            \begin{table}[ht!]
                \begin{tabular}{| r |}
                    \hline
                    \mycommand{} \\
                    \hline
                \end{tabular}
            \end{table}
        
            \begin{table}[ht!]
                \begin{tabular}{| r |}
                    \hline
                    \mycommand{}          \\
                    \hline
                \end{tabular}
            \end{table}
        
            Some may wonder why \mycommand{} is so special.
        \end{document}
    """, true
    )

    fun testWithQuotes() = doTest(
        """
        We could not find strategies that would be of great assistance in this category.
	    However, if you ever find yourself reading ``<selection>Test With Quotes</selection>'' I thinnk you for your service.
    """, emptyList(), 0, """
        \newcommand{\mycommand}{Test With Quotes}
        
        We could not find strategies that would be of great assistance in this category.
	    However, if you ever find yourself reading ``\mycommand{}'' I thinnk you for your service.
    """
    )

    private fun doTest(
        before: String,
        expressions: List<String>,
        target: Int,
        after: String,
        replaceAll: Boolean = false,
    ) {
        var shownTargetChooser = false
        withMockTargetExpressionChooser(object : ExtractExpressionUi {
            override fun chooseTarget(exprs: List<PsiElement>): PsiElement {
                shownTargetChooser = true
                assertEquals(exprs.map { it.text }, expressions)
                return exprs[target]
            }

            override fun chooseOccurrences(expr: PsiElement, occurrences: List<PsiElement>): List<PsiElement> =
                if (replaceAll) occurrences else listOf(expr)
        }) {
            myFixture.configureByText(LatexFileType, before.trimIndent())
            myFixture.performEditorAction("IntroduceVariable")
            myFixture.checkResult(after.trimIndent())

            check(expressions.isEmpty() || shownTargetChooser) {
                "Chooser isn't shown"
            }
        }
    }
}