package nl.hannahsten.texifyidea.inspections.latex

import nl.hannahsten.texifyidea.file.LatexFileType
import nl.hannahsten.texifyidea.inspections.TexifyInspectionTestBase
import nl.hannahsten.texifyidea.inspections.latex.codestyle.LatexNonBreakingSpaceInspection
import nl.hannahsten.texifyidea.testutils.writeCommand

class LatexNonBreakingSpaceInspectionTest : TexifyInspectionTestBase(LatexNonBreakingSpaceInspection()) {

    fun testWarning() {
        myFixture.configureByText(
            LatexFileType,
            """
            Reference<warning descr="Reference without a non-breaking space"> </warning>\ref{fig}
            """.trimIndent()
        )
        myFixture.checkHighlighting()
    }

    fun testNoWarning() {
        myFixture.configureByText(
            LatexFileType,
            """
                Reference~\ref{fig}
            """.trimIndent()
        )
        myFixture.checkHighlighting()
    }

    fun `test no warning when reference in required parameter`() {
        myFixture.configureByText(
            LatexFileType,
            """
            abc \dummy{Reference~\ref{fig}}
            """.trimIndent()
        )
        myFixture.checkHighlighting()
    }

    fun testQuickFix() {
        myFixture.configureByText(
            LatexFileType,
            """
            Reference \ref{fig}
            """.trimIndent()
        )

        val quickFixes = myFixture.getAllQuickFixes()
        assertEquals(1, quickFixes.size)
        writeCommand(myFixture.project) {
            quickFixes.first().invoke(myFixture.project, myFixture.editor, myFixture.file)
        }

        myFixture.checkResult(
            """
            Reference~\ref{fig}
            """.trimIndent()
        )
    }
}