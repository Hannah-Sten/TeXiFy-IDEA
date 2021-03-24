package nl.hannahsten.texifyidea.inspections.latex

import nl.hannahsten.texifyidea.inspections.TexifyInspectionTestBase
import nl.hannahsten.texifyidea.inspections.latex.typesetting.LatexDiacriticIJInspection

class LatexDiacriticIJInspectionTest : TexifyInspectionTestBase(LatexDiacriticIJInspection()) {

    fun `test warning`() = testHighlighting("""<warning>\^i</warning>""")

    fun `test no warning`() = testHighlighting("""\^{\i}""")

    fun `test quick fix i`() = testQuickFix(
            before = """\^i""",
            after = """\^{\i}"""
    )

    fun `test quick fix j`() = testQuickFix(
            before = """\^j""",
            after = """\^{\j}"""
    )

    fun `test quick fix i in math mode`() = testQuickFix(
            before = """$\^i$""",
            after = """$\^\imath$"""
    )

    fun `test quick fix j in math mode`() = testQuickFix(
            before = """$\^j$""",
            after = """$\^\jmath$"""
    )
}