package nl.hannahsten.texifyidea.inspections.latex

import nl.hannahsten.texifyidea.inspections.TexifyInspectionTestBase
import nl.hannahsten.texifyidea.inspections.latex.codematurity.LatexOverInsteadOfFracInspection

class LatexOverInsteadOfFracInspectionTest : TexifyInspectionTestBase(LatexOverInsteadOfFracInspection()) {

    fun `test over warning`() = testHighlighting("""$1 <warning descr="Use of \over is discouraged">\over</warning> 2$""")

    fun `test frac no warning`() = testHighlighting("""$\frac{1}{2}$""")

    fun `test quick fix`() = testQuickFix(
            before = """$1\over 2$""",
            after = """$\frac{1}{2}$""".trimMargin()
    )

    fun `test quick fix without space`() = testQuickFix(
            before = """$1\over2$""",
            after = """$\frac{1}{2}$""".trimMargin()
    )

    fun `test quick fix without numerator and denominator`() = testQuickFix(
            before = """$\over$""",
            after = """$\frac{}{}$"""
    )

    fun `test quick fix in formula`() = testQuickFix(
            before = """${'$'}x = {1\over2} + y$""",
            after = """${'$'}x = {\frac{1}{2}} + y$""".trimMargin()
    )
}