package nl.hannahsten.texifyidea.inspections.latex.typesetting.spacing

import nl.hannahsten.texifyidea.inspections.TexifyInspectionTestBase

class LatexMathOperatorEscapeInspectionTest : TexifyInspectionTestBase(LatexMathOperatorEscapeInspection()) {

    fun `test inspection triggered in inline math`() {
        testHighlighting("""Hallo ${'$'}y = <warning descr="Non-escaped math operator">cos</warning>(x)${'$'}""")
    }

    fun `test no trigger outside math mode`() {
        testHighlighting("cos")
    }

    fun `test trigger in math inside text`() {
        testHighlighting(
            """
            \[
                \begin{cases}
                    1 & \text{if ${'$'}<warning descr="Non-escaped math operator">cos</warning>(x) = 1${'$'}} \\
                    0 & \text{otherwise}
                \end{cases}
            \]
            """.trimIndent()
        )
    }

    fun `test no trigger inside text in inline math`() {
        testHighlighting("""Hallo ${'$'}\text{cos}(x)${'$'}""")
    }
}