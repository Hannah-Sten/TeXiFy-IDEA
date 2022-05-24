package nl.hannahsten.texifyidea.inspections.latex.codematurity

import nl.hannahsten.texifyidea.inspections.TexifyInspectionTestBase

class LatexDiscouragedUseOfDefInspectionTest : TexifyInspectionTestBase(LatexDiscouragedUseOfDefInspection()) {

    fun `test warning for def`() = testHighlighting("""<warning descr="The use of TeX primitive \def is discouraged">\def</warning>\a1""")

    fun `test warning for let`() = testHighlighting("""<warning descr="The use of TeX primitive \let is discouraged">\let</warning>\a1""")

    fun `test quick fix def to newcommand`() = testNamedQuickFix(
            before = """\def\a1""",
            after = """\newcommand{\a}{1}""",
            numberOfFixes = 2,
            quickFixName = "Convert to \\newcommand"
    )

    fun `test quick fix def to newcommand 2`() = testNamedQuickFix(
            before = """
                \def \indentpar {\hangindent=1cm \hangafter=0}
                \setlength{\parskip}{0.5em}
            """.trimIndent(),
            after = """
                \newcommand{\indentpar}{\hangindent=1cm \hangafter=0}
                \setlength{\parskip}{0.5em}
            """.trimIndent(),
            numberOfFixes = 2,
            quickFixName = "Convert to \\newcommand"
    )

    fun `test quick fix def to renewcommand`() = testNamedQuickFix(
            before = """\def\a1""",
            after = """\renewcommand{\a}{1}""",
            numberOfFixes = 2,
            quickFixName = "Convert to \\renewcommand"
    )

    fun `test quick fix let to newcommand`() = testNamedQuickFix(
            before = """\let\a1""",
            after = """\newcommand{\a}{1}""",
            numberOfFixes = 2,
            quickFixName = "Convert to \\newcommand"
    )

    fun `test quick fix let to renewcommand`() = testNamedQuickFix(
            before = """\let\a1""",
            after = """\renewcommand{\a}{1}""",
            numberOfFixes = 2,
            quickFixName = "Convert to \\renewcommand"
    )
}