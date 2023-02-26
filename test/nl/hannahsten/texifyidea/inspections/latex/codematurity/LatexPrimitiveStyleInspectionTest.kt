package nl.hannahsten.texifyidea.inspections.latex.codematurity

import nl.hannahsten.texifyidea.file.LatexFileType
import nl.hannahsten.texifyidea.inspections.TexifyInspectionTestBase

class LatexPrimitiveStyleInspectionTest : TexifyInspectionTestBase(LatexPrimitiveStyleInspection()) {

    fun testWarning() {
        myFixture.configureByText(
            LatexFileType,
            """
            {<warning descr="Use of TeX primitive \bf is discouraged">\bf</warning> is bold}
            """.trimIndent()
        )
        myFixture.checkHighlighting()
    }

    fun `test simple quickfix`() {
        testQuickFix("""{\it is italic}""", """\textit{is italic}""")
    }

    fun `test quickfix in group`() {
        testQuickFix("""{help abc \it is italic}""", """help abc \textit{is italic}""")
    }

    fun `test bf`() {
        myFixture.configureByText(
            LatexFileType,
            """
            \begin{center}
            {\Large <warning descr="Use of TeX primitive \bf is discouraged">\bf{Instructions for formatting (S)PC list}</warning>}
            \end{center}

            The format of the text file should be as follows. \\
            """.trimIndent()
        )
        myFixture.checkHighlighting()
    }

    fun `test quick fix`() {
        testQuickFix(
            """
            \begin{center}
            {\Large \bf{Instructions for formatting (S)PC list}}
            \end{center}

            The format of the text file should be as follows. \\
            """.trimIndent(),
            """
            \begin{center}
            {\Large \textbf{Instructions for formatting (S)PC list}}
            \end{center}

            The format of the text file should be as follows. \\
            """.trimIndent()
        )
    }
}