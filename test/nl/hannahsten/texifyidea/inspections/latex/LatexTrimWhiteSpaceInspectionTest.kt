package nl.hannahsten.texifyidea.inspections.latex

import nl.hannahsten.texifyidea.inspections.TexifyInspectionTestBase

class LatexTrimWhiteSpaceInspectionTest : TexifyInspectionTestBase(LatexTrimWhitespaceInspection()) {

    fun `test no warning in section command`() = testHighlighting("""\section{test}""")

    fun `test no warning in non-section command`() = testHighlighting("""\box{ test}""")

    fun `test warning whitespace front`() = testHighlighting("""\section{<warning descr="Unnecessary whitespace"> test</warning>}""")

    fun `test warning whitespace back`() = testHighlighting("""\section{<warning descr="Unnecessary whitespace">test </warning>}""")

    fun `test warning whitespace front and back`() = testHighlighting("""\section{<warning descr="Unnecessary whitespace"> test </warning>}""")

    fun `test quick fix front`() = testQuickFix(
            before = """\section{ test}""",
            after = """\section{test}"""
    )

    fun `test quick fix back`() = testQuickFix(
            before = """\section{test }""",
            after = """\section{test}"""
    )

    fun `test quick fix front and back`() = testQuickFix(
            before = """\section{ test }""",
            after = """\section{test}"""
    )
}