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

    fun `test empty quickfix`() {
        testQuickFix("""\it""", """\textit""")
    }

    fun `test implicit required argument quickfix`() {
        testQuickFix("""\it test""", """\textit test""")
    }

    fun `test quickfix in group`() {
        testQuickFix("""{help abc \it is italic}""", """help abc \textit{is italic}""")
    }

    fun `test quickfix in parameter`() {
        testQuickFix("""{ text \noindent {\bf Applications.} }""", """{ text \noindent \textbf{Applications.} }""")
    }

    fun `test file`() {
        testQuickFix(
            """
            \documentclass{article}

            \begin{document}
                asdf {my \bf bold text} not bold
            \end{document}
            """.trimIndent(),
            """
            \documentclass{article}

            \begin{document}
                asdf my \textbf{bold text} not bold
            \end{document}
            """.trimIndent()
        )
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