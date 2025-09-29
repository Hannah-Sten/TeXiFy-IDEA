package nl.hannahsten.texifyidea.inspections.latex.typesetting.spacing

import nl.hannahsten.texifyidea.file.LatexFileType
import nl.hannahsten.texifyidea.inspections.TexifyInspectionTestBase
import nl.hannahsten.texifyidea.updateCommandDef

class LatexMathOperatorEscapeInspectionTest : TexifyInspectionTestBase(LatexMathOperatorEscapeInspection()) {

    fun `test inspection triggered in inline math`() {
        testHighlighting("""Hallo ${'$'}y = <warning descr="Non-escaped math operator">cos</warning>(x)$""")
    }

    fun `test no trigger outside math mode`() {
        testHighlighting("cos")
    }

    fun `test trigger in math inside text`() {
        testHighlighting(
            """
            \[
                \begin{cases}
                    1 & \text{if $<warning descr="Non-escaped math operator">cos</warning>(x) = 1$} \\
                    0 & \text{otherwise}
                \end{cases}
            \]
            """.trimIndent()
        )
    }

    fun `test no trigger inside text in inline math`() {
        myFixture.configureByText(
            LatexFileType,
            """
            \usepackage{amstext}
            Hallo $\text{cos}(x)$
            """.trimIndent()
        )
        // technically speaking, \text command is contained in amstext package (contained in amsmath)
        // so we have to update the command definitions for this test to work
        myFixture.updateCommandDef()
        myFixture.checkHighlighting()
    }
}