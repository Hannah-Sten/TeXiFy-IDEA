package nl.hannahsten.texifyidea.inspections.latex.probablebugs

import nl.hannahsten.texifyidea.file.LatexFileType
import nl.hannahsten.texifyidea.inspections.TexifyInspectionTestBase
import nl.hannahsten.texifyidea.testutils.writeCommand

class LatexRequiredExtensionInspectionTest : TexifyInspectionTestBase(LatexRequiredExtensionInspection()) {

    fun testWarning() {
        myFixture.configureByText(
            LatexFileType,
            """
            \addbibresource{<error descr="File argument should include the extension">test</error>}
            """.trimIndent()
        )
        myFixture.checkHighlighting()
    }

    fun testNoWarning() {
        myFixture.configureByText(
            LatexFileType,
            """
            \addbibresource{test.bib}
            """.trimIndent()
        )
        myFixture.checkHighlighting()
    }

    fun testQuickfix() {
        myFixture.configureByText(
            LatexFileType,
            """
            \documentclass{article}
            \addbibresource{test}
            """.trimIndent()
        )

        val quickFixes = myFixture.getAllQuickFixes()
        assertEquals(1, quickFixes.size)
        writeCommand(myFixture.project) {
            quickFixes.first().invoke(myFixture.project, myFixture.editor, myFixture.file)
        }

        myFixture.checkResult(
            """
            \documentclass{article}
            \addbibresource{test.bib}
            """.trimIndent()
        )
    }
}