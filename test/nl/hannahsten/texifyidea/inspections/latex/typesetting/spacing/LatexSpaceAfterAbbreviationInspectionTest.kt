package nl.hannahsten.texifyidea.inspections.latex.typesetting.spacing

import nl.hannahsten.texifyidea.file.LatexFileType
import nl.hannahsten.texifyidea.inspections.TexifyInspectionTestBase
import nl.hannahsten.texifyidea.testutils.writeCommand

class LatexSpaceAfterAbbreviationInspectionTest : TexifyInspectionTestBase(LatexSpaceAfterAbbreviationInspection()) {

    fun testWarning() {
        myFixture.configureByText(
            LatexFileType,
            """
            e.g<weak_warning descr="Abbreviation should be followed by a normal space">. </weak_warning>text
            """.trimIndent()
        )
        myFixture.checkHighlighting()
    }

    fun testNoWarning() {
        myFixture.configureByText(
            LatexFileType,
            """
            e.g.\ text and a. also end of sentence. But.
            """.trimIndent()
        )
        myFixture.checkHighlighting()
    }

    fun testNoWarningNonBreakingSpace() {
        myFixture.configureByText(
            LatexFileType,
            """
            e.g.~text
            """.trimIndent()
        )
        myFixture.checkHighlighting()
    }

    fun testQuickfix() {
        myFixture.configureByText(
            LatexFileType,
            """
            e.g. test
            """.trimIndent()
        )

        val quickFixes = myFixture.getAllQuickFixes()
        assertEquals(1, quickFixes.size)
        writeCommand(myFixture.project) {
            quickFixes.first().invoke(myFixture.project, myFixture.editor, myFixture.file)
        }

        myFixture.checkResult(
            """
            e.g.\ test
            """.trimIndent()
        )
    }
}