package nl.hannahsten.texifyidea.inspections.latex

import nl.hannahsten.texifyidea.inspections.TexifyInspectionTestBase
import nl.hannahsten.texifyidea.inspections.latex.codematurity.LatexAvoidEqnarrayInspection

class LatexAvoidEqnarrayInspectionTest : TexifyInspectionTestBase(LatexAvoidEqnarrayInspection()) {

    fun `test eqnarray warning`() = testHighlighting("""
        \begin{<warning descr="Avoid using the 'eqnarray' environment">eqnarray</warning>}
            x
        \end{eqnarray}
    """.trimIndent())

    fun `test eqnarray star warning`() = testHighlighting("""
        \begin{<warning descr="Avoid using the 'eqnarray*' environment">eqnarray*</warning>}
            x
        \end{eqnarray*}
    """.trimIndent())

    fun `test no warning for other math environment`() = testHighlighting("""
        \begin{align}
            x
        \end{align}
    """.trimIndent())

    fun `test quick fix`() = testQuickFix(
            before = """
                \usepackage{amsmath}
                \begin{eqnarray*}
                    x
                \end{eqnarray*}
            """.trimIndent(),
            after = """
                \usepackage{amsmath}
                \begin{align*}
                    x
                \end{align*}
            """.trimIndent()
    )
}