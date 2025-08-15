package nl.hannahsten.texifyidea.completion

import com.intellij.codeInsight.completion.CompletionType
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import nl.hannahsten.texifyidea.file.LatexFileType
import nl.hannahsten.texifyidea.updateCommandDef

class ContextAwareCompletionTest : BasePlatformTestCase() {

    fun testComposeArgument() {
        myFixture.configureByText(
            LatexFileType,
            """
            \documentclass{article}
            \usepackage{xcolor}
            \usepackage{hyperref}
            \newcommand{\myColoredRef}[2][blue]{\textcolor{#1}{\ref{#2}}}
            \begin{document}
            
            \section{Introduction}\label{sec:intro}
            \myColoredRef[re<caret>]{sec:intro}
            
            \end{document}
            """.trimIndent()
        )
        myFixture.updateCommandDef()

        val res = myFixture.completeBasic() // red or green should be suggested here
        assertNotNull("Completion should not be null", res)
        assertTrue("Completion should contain 'red'", res.any { it.lookupString == "red" })
    }

    fun testArgSpecCommands() {
        myFixture.configureByText(
            LatexFileType,
            """
            \documentclass{article}
            \usepackage{xparse}
            \DeclareDocumentCommand{\mytest}{oO{momo}moo}{nothing} % O{momo} is an optional argument with default value 'momo'
            
            \myt<caret>
            """.trimIndent()
        )
        myFixture.updateCommandDef()
        val res = myFixture.complete(CompletionType.BASIC)
        assertNotNull("Completion should not be null", res)
        assertEquals(3 * 3, res.size) // [ , o, oO] * [m] * [ , o, oo] = 9 completions
    }
}
