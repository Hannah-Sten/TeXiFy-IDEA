package nl.hannahsten.texifyidea.inspections.latex.codematurity

import nl.hannahsten.texifyidea.file.LatexFileType
import nl.hannahsten.texifyidea.inspections.TexifyInspectionTestBase
import nl.hannahsten.texifyidea.testutils.writeCommand
import nl.hannahsten.texifyidea.updateCommandDef

class LatexAvoidEqnarrayInspectionTest : TexifyInspectionTestBase(LatexAvoidEqnarrayInspection()) {

    fun `test eqnarray warning`() = testHighlighting(
        """
        \begin{<warning descr="Avoid using the 'eqnarray' environment">eqnarray</warning>}
            x
        \end{eqnarray}
        """.trimIndent()
    )

    fun `test eqnarray star warning`() = testHighlighting(
        """
        \begin{<warning descr="Avoid using the 'eqnarray*' environment">eqnarray*</warning>}
            x
        \end{eqnarray*}
        """.trimIndent()
    )

    fun `test no warning for other math environment`() = testHighlighting(
        """
        \begin{align}
            x
        \end{align}
        """.trimIndent()
    )

    fun `test quick fix`() {
        myFixture.configureByText(
            LatexFileType,
            """
                \usepackage{amsmath}
                \begin{eqnarray*}
                    x
                \end{eqnarray*}
            """.trimIndent()
        )
        myFixture.updateCommandDef()
        val quickFixes = myFixture.getAllQuickFixes()
        assertEquals("Expected number of quick fixes:", 1, quickFixes.size)
        writeCommand(myFixture.project) {
            quickFixes.first().invoke(myFixture.project, myFixture.editor, myFixture.file)
        }
        myFixture.checkResult(
            """
                \usepackage{amsmath}
                \begin{align*}
                    x
                \end{align*}
            """.trimIndent()
        )
    }
}