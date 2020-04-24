package nl.hannahsten.texifyidea.inspections.latex

import nl.hannahsten.texifyidea.inspections.TexifyInspectionTriggerTestBase

class UnescapedHashInspectionTest : TexifyInspectionTriggerTestBase(LatexEscapeHashOutsideCommandInspection()) {
    override val triggers: List<String> = listOf(
            """#""",
            """#3""",
            """Some normal text #boring"""
    )

    override val noTriggers: List<String> = listOf(
            """\newcommand{\a}[1]{#1}""",
            """\NewDocumentCommand{\b}{m}{#1}""",
            """\newcommand{\c}[1]{\textbf{#1}}"""
    )
}