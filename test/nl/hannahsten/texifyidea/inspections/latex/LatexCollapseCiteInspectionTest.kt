package nl.hannahsten.texifyidea.inspections.latex

import nl.hannahsten.texifyidea.file.LatexFileType
import nl.hannahsten.texifyidea.inspections.TexifyInspectionTestBase
import nl.hannahsten.texifyidea.testutils.writeCommand

class LatexCollapseCiteInspectionTest : TexifyInspectionTestBase(LatexCollapseCiteInspection()) {
    fun testWarning() {
        myFixture.configureByText(LatexFileType, """
            <warning descr="Citations can be collapsed">\cite{knuth1990}</warning><warning descr="Citations can be collapsed">\cite{goossens1993}</warning>
        """.trimIndent())
        myFixture.checkHighlighting()
    }

    fun testQuickfix() {
        myFixture.configureByText(LatexFileType,
        """
            \cite{knuth1990}\cite{goossens1993}
        """.trimIndent())

        val quickFixes = myFixture.getAllQuickFixes()
        assertEquals(2, quickFixes.size)
        writeCommand(myFixture.project) {
            quickFixes.first().invoke(myFixture.project, myFixture.editor, myFixture.file)
        }

        myFixture.checkResult("""
            \cite{knuth1990,goossens1993}
        """.trimIndent())
    }
}