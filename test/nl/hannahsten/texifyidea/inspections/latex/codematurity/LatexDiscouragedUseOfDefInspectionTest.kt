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

    fun `test quick fix def in newenvironment`() = testNamedQuickFix(
        before = """
            \newenvironment{hasdef}{
            	\def\skittle{nevis}
            	\begin{center}
            }{
            	\end{center}
            }
        """.trimIndent(),
        after = """
            \newenvironment{hasdef}{
            	\newcommand{\skittle}{nevis}
            	\begin{center}
            }{
            	\end{center}
            }
        """.trimIndent(),
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

    fun `test quick fix def with argument`() = testNamedQuickFix(
        before = """\def\testa#1{#1}""",
        after = """\newcommand{\testa}[1]{#1}""",
        numberOfFixes = 2,
        quickFixName = "Convert to \\newcommand"
    )

    fun `test quick fix def with multiple arguments`() = testNamedQuickFix(
        before = """\def\testa#1#2#3#4{#3#4}""",
        after = """\newcommand{\testa}[4]{#3#4}""",
        numberOfFixes = 2,
        quickFixName = "Convert to \\newcommand"
    )

    /**
     * \newcommand doesn't actually support this, but the inspection is triggered and this seems like the most sensible thing to do.
     */
    fun `test quick fix def with argument until next brace`() = testNamedQuickFix(
        before = """\def\testa#1#{#1}""",
        after = """\newcommand{\testa}[1]{#1}""",
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