package nl.hannahsten.texifyidea.inspections.latex.codestyle

import nl.hannahsten.texifyidea.file.LatexFileType
import nl.hannahsten.texifyidea.inspections.TexifyInspectionTestBase
import nl.hannahsten.texifyidea.testutils.writeCommand

class LatexEquationReferenceInspectionTest : TexifyInspectionTestBase(LatexEquationReferenceInspection()) {

    fun testWarning() {
        myFixture.configureByText(
            LatexFileType,
            """
            \begin{equation}
            \label{eq:time3}
            \end{equation}
            test constraints <weak_warning descr="Use \eqref">(\ref{eq:time3})</weak_warning> enforce node
            """.trimIndent()
        )
        myFixture.checkHighlighting()
    }

    fun testQuickfix() {
        myFixture.configureByText(
            LatexFileType,
            """
            \begin{equation}
            \label{eq:time3}
            \end{equation}
            test constraints (\ref{eq:time3}) enforce node
            """.trimIndent()
        )

        val quickFixes = myFixture.getAllQuickFixes()
        assertEquals(1, quickFixes.size)
        writeCommand(myFixture.project) {
            quickFixes.first().invoke(myFixture.project, myFixture.editor, myFixture.file)
        }

        myFixture.checkResult(
            """
            \usepackage{amsmath}\begin{equation}
            \label{eq:time3}
            \end{equation}
            test constraints \eqref{eq:time3} enforce node
            """.trimIndent()
        )
    }
}