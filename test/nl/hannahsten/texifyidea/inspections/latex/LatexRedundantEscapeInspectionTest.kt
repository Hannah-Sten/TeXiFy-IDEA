package nl.hannahsten.texifyidea.inspections.latex

import nl.hannahsten.texifyidea.file.LatexFileType
import nl.hannahsten.texifyidea.inspections.TexifyInspectionTestBase
import nl.hannahsten.texifyidea.testutils.writeCommand

class LatexRedundantEscapeInspectionTest : TexifyInspectionTestBase(LatexRedundantEscapeInspection()) {
    fun testWarning() {
        myFixture.configureByText(LatexFileType, """
            <weak_warning descr="Redundant diacritic escape">\'</weak_warning>u
        """.trimIndent())
        myFixture.checkHighlighting()
    }

    fun testNoWarning() {
        myFixture.configureByText(LatexFileType, """
            ú
        """.trimIndent())
        myFixture.checkHighlighting()
    }

    fun testQuickfix() {
        myFixture.configureByText(LatexFileType,
        """
            \'u
        """.trimIndent())

        val quickFixes = myFixture.getAllQuickFixes()
        assertEquals(1, quickFixes.size)
        writeCommand(myFixture.project) {
            quickFixes.first().invoke(myFixture.project, myFixture.editor, myFixture.file)
        }

        myFixture.checkResult("""
            ú
        """.trimIndent())
    }
}