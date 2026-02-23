package nl.hannahsten.texifyidea.inspections.latex.probablebugs

import nl.hannahsten.texifyidea.file.LatexFileType
import nl.hannahsten.texifyidea.inspections.TexifyInspectionTestBase
import nl.hannahsten.texifyidea.testutils.writeCommand

class LatexNoExtensionInspectionTest : TexifyInspectionTestBase(LatexNoExtensionInspection()) {

    fun testWarning() {
        myFixture.configureByText(
            LatexFileType,
            """
            \bibliography{<warning descr="File argument should not include the extension">test.bib</warning>}
            \includegraphics[lots of options here]{<warning descr="File argument should not include the extension">folder/file.png</warning>}
            """.trimIndent()
        )
        myFixture.checkHighlighting()
    }

    fun testNoWarning() {
        myFixture.configureByText(
            LatexFileType,
            """
            \bibliography{test}
            """.trimIndent()
        )
        myFixture.checkHighlighting()
    }

    fun testQuickfix() {
        myFixture.configureByText(
            LatexFileType,
            """
            \begin{document}
            \bibliography{test.bib,test2.bib}
            \end{document}
            """.trimIndent()
        )

        val quickFixes = myFixture.getAllQuickFixes()
        assertEquals(2, quickFixes.size)
        writeCommand(myFixture.project) {
            quickFixes.first().invoke(myFixture.project, myFixture.editor, myFixture.file)
        }

        myFixture.checkResult(
            """
            \begin{document}
            \bibliography{test,test2}
            \end{document}
            """.trimIndent()
        )
    }
}