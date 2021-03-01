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
            % All beds diverged by the stated variants <caret>can be written by the algorithm.
        """)
        myFixture.type("\n")
        myFixture.checkResult("""
            % All beds diverged by the stated variants 
            <caret>% can be written by the algorithm.
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
}