package nl.hannahsten.texifyidea.editor.typedhandlers

import com.intellij.testFramework.fixtures.BasePlatformTestCase
import nl.hannahsten.texifyidea.file.LatexFileType

class LatexEnterInCommentHandlerTest : BasePlatformTestCase() {

    fun testSimpleComment() {
        myFixture.configureByText(LatexFileType, """
            % These forests are to be indicated by the length <caret>according to their truthful measures.
        """.trimIndent())
        myFixture.type("\n") // It actually presses enter: see EditorTestFixture.java:91
        myFixture.checkResult("""
            % These forests are to be indicated by the length 
            <caret>% according to their truthful measures.
        """.trimIndent())
    }

    fun testCommentWithIndentation() {
        // For example, when having the setting "line comment at first column" unselected
        myFixture.configureByText(LatexFileType, """
\begin{document}
    \begin{center}
        % All beds diverged by the stated variants <caret>can be written by the algorithm.
    \end{center}
\end{document}
        """)
        myFixture.type("\n")
        myFixture.checkResult("""
\begin{document}
    \begin{center}
        % All beds diverged by the stated variants 
        <caret>% can be written by the algorithm.
    \end{center}
\end{document}
        """)
    }

    fun testCommentAtFirstColumn() {
        myFixture.configureByText(LatexFileType, """
            %   And to state attentions to the purpose <caret>to sin the clear found by genes.
        """.trimIndent())
        myFixture.type("\n")
        myFixture.checkResult("""
            %   And to state attentions to the purpose 
            <caret>%   to sin the clear found by genes.
        """.trimIndent())
    }

    fun testMagicComment() {
        myFixture.configureByText(LatexFileType, """
            %! compiler=<caret>lualatex
        """.trimIndent())
        myFixture.type("\n")
        myFixture.checkResult("""
            %! compiler=
            <caret>%! lualatex
        """.trimIndent())
    }

    fun testDoublePercent() {
        myFixture.configureByText(LatexFileType, """
            \begin{center}
            %%     It would previously <caret>repair the redundant persons.
            \end{center}
        """.trimIndent())
        myFixture.type("\n")
        myFixture.checkResult("""
            \begin{center}
            %%     It would previously 
            %%     repair the redundant persons.
            \end{center}
        """.trimIndent())
    }

    fun testFakeComment() {
        myFixture.configureByText(LatexFileType, """
            \macro{%<caret>}
        """.trimIndent())
        myFixture.type("\n")
        myFixture.checkResult("""
            \macro{%
            }
        """.trimIndent())
    }
}