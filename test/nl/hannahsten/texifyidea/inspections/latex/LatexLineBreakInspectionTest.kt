package nl.hannahsten.texifyidea.inspections.latex

import nl.hannahsten.texifyidea.file.LatexFileType
import nl.hannahsten.texifyidea.inspections.TexifyInspectionTestBase
import nl.hannahsten.texifyidea.testutils.writeCommand

class LatexLineBreakInspectionTest : TexifyInspectionTestBase(LatexLineBreakInspection()) {
    fun testWarning() {
        myFixture.configureByText(LatexFileType, """
            Not this, b<weak_warning descr="Sentence does not start on a new line">ut. This starts a new line.</weak_warning>
This e<weak_warning descr="Sentence does not start on a new line">tc. is missing a normal space, but i.e. this etc.</weak_warning>\ is not.
            % not. in. comments
        """.trimIndent())
        myFixture.checkHighlighting()
    }

    fun testNoWarning() {
        myFixture.configureByText(LatexFileType, """
            First sentence.
            Second sentence.
        """.trimIndent())
        myFixture.checkHighlighting()
    }

    fun testNoWarningWithComment() {
        myFixture.configureByText(LatexFileType, """
            This is an abbreviation (ABC). % commemt
            More text here.
        """.trimIndent())
        myFixture.checkHighlighting()
    }

    fun testNoWarningInMathMode() {
        myFixture.configureByText(LatexFileType, """
            \[ Why. would. you. do. this. \]
        """.trimIndent())
        myFixture.checkHighlighting()
    }

    fun testQuickfix() {
        myFixture.configureByText(LatexFileType,
        """
            I end. a sentence.
        """.trimIndent())

        val quickFixes = myFixture.getAllQuickFixes()
        assertEquals(1, quickFixes.size)
        writeCommand(myFixture.project) {
            quickFixes.first().invoke(myFixture.project, myFixture.editor, myFixture.file)
        }

        myFixture.checkResult("""
            I end.
            a sentence.
        """.trimIndent())
    }
}