package nl.hannahsten.texifyidea.inspections.latex.codestyle

import nl.hannahsten.texifyidea.file.LatexFileType
import nl.hannahsten.texifyidea.inspections.TexifyInspectionTestBase
import nl.hannahsten.texifyidea.inspections.latex.codematurity.LatexPrimitiveStyleInspection

class LatexPrimitiveStyleInspectionTest : TexifyInspectionTestBase(LatexPrimitiveStyleInspection()) {

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