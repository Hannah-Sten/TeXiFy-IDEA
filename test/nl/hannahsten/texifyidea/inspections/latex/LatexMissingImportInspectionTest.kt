package nl.hannahsten.texifyidea.inspections.latex

import nl.hannahsten.texifyidea.file.LatexFileType
import nl.hannahsten.texifyidea.inspections.TexifyInspectionTestBase
import nl.hannahsten.texifyidea.testutils.writeCommand

class LatexMissingImportInspectionTest : TexifyInspectionTestBase(LatexMissingImportInspection()) {
    fun testWarning() {
        myFixture.configureByText(
            LatexFileType,
            """
            <error descr="Command requires xcolor package">\color</error>{blue}
            """.trimIndent()
        )
        myFixture.checkHighlighting()
    }

    fun testQuickfix() {
        myFixture.configureByText(
            LatexFileType,
            """
            \documentclass{article}

            \usepackage{amsmath}

            \begin{document}
                \color{blue}
            \end{document}
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

            \usepackage{amsmath}
            \usepackage{xcolor}

            \begin{document}
                \color{blue}
            \end{document}
            """.trimIndent()
        )
    }
}