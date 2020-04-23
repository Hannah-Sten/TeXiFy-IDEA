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
            """\newcommand{\c}[1]{\textbf{#1}}""",
            """\newcommand*\eval[1]{\left.#1\right\rvert}""",
            """
                \newcommand\restr[2]{{
                    \left.\kern-\nulldelimiterspace % automatically resize the bar with \right
                    #1 % the function
                    \vphantom{\big|} % pretend it's a little taller at normal size
                    \right|_{#2} % this is the delimiter
                }}
        
                \newcommand*\circled[1]{\tikz[baseline=(char.base)]{
                    \node[shape=circle,draw,inner sep=2pt] (char) {#1};
                }}
        
                \makeatletter
                \renewcommand*\env@matrix[1][*\c@MaxMatrixCols c]{%
                    \hskip -\arraycolsep
                    \let\@ifnextchar\new@ifnextchar
                    \array{#1}}
                \makeatother
            """.trimIndent()
    )
}