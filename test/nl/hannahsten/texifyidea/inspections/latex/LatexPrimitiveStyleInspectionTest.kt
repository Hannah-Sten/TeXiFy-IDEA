package nl.hannahsten.texifyidea.inspections.latex

import nl.hannahsten.texifyidea.file.LatexFileType
import nl.hannahsten.texifyidea.inspections.TexifyInspectionTestBase
import nl.hannahsten.texifyidea.testutils.writeCommand

class LatexPrimitiveStyleInspectionTest : TexifyInspectionTestBase(LatexPrimitiveStyleInspection()) {
    fun testWarning() {
        myFixture.configureByText(LatexFileType, """
            {\bf is bold}
        """.trimIndent())
        myFixture.checkHighlighting()
    }

    fun testQuickfix() {
        myFixture.configureByText(LatexFileType,
        """
            {\it is italic}
        """.trimIndent())

        val quickFixes = myFixture.getAllQuickFixes()
        assertEquals(1, quickFixes.size)
        writeCommand(myFixture.project) {
            quickFixes.first().invoke(myFixture.project, myFixture.editor, myFixture.file)
        }

        myFixture.checkResult("""
            {\it is italic}
        """.trimIndent())
    }
}