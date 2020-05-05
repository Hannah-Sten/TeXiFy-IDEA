package nl.hannahsten.texifyidea.inspections.latex

import nl.hannahsten.texifyidea.file.LatexFileType
import nl.hannahsten.texifyidea.inspections.TexifyInspectionTestBase
import nl.hannahsten.texifyidea.testutils.writeCommand

class LatexNoExtensionInspectionTest : TexifyInspectionTestBase(LatexNoExtensionInspection()) {
    fun testWarning() {
        myFixture.configureByText(LatexFileType, """
            \bibliography{<error descr="File argument should not include the extension">test.bib</error>}
        """.trimIndent())
        myFixture.checkHighlighting()
    }

    fun testNoWarning() {
        myFixture.configureByText(LatexFileType, """
            \bibliography{test}
        """.trimIndent())
        myFixture.checkHighlighting()
    }

    fun testQuickfix() {
        myFixture.configureByText(LatexFileType,
        """
            \bibliography{test.bib}
        """.trimIndent())

        val quickFixes = myFixture.getAllQuickFixes()
        assertEquals(1, quickFixes.size)
        writeCommand(myFixture.project) {
            quickFixes.first().invoke(myFixture.project, myFixture.editor, myFixture.file)
        }

        myFixture.checkResult("""
            \bibliography{test}
        """.trimIndent())
    }
}