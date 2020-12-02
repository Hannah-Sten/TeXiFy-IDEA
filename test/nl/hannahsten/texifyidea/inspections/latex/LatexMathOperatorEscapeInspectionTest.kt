package nl.hannahsten.texifyidea.inspections.latex

import nl.hannahsten.texifyidea.inspections.TexifyInspectionTestBase

class LatexMathOperatorEscapeInspectionTest : TexifyInspectionTestBase(LatexMathOperatorEscapeInspection()) {
    fun `test inspection triggered in inline math`() {
        testHighlighting("""Hallo ${'$'}y = <warning descr="Non-escaped math operator">cos</warning>(x)${'$'}""")
    }

    fun `test no trigger outside math mode`() {
        testHighlighting("cos")
    }

    fun `test no trigger inside text in inline math`() {
        testHighlighting("""Hallo ${'$'}\text{cos}(x)${'$'}""")
    }
}