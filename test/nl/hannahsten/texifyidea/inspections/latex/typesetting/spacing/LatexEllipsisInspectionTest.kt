package nl.hannahsten.texifyidea.inspections.latex.typesetting.spacing

import nl.hannahsten.texifyidea.inspections.TexifyInspectionTestBase
import nl.hannahsten.texifyidea.updateCommandDef

class LatexEllipsisInspectionTest : TexifyInspectionTestBase(LatexEllipsisInspection()) {

    fun testInspections() {
        myFixture.configureByText(
            "main.tex",
            """
                \documentclass{article}
                \begin{document}
                  <warning descr="Ellipsis with ... instead of \ldots or \dots">...</warning>
                  \begin{equation}
                    a + b + c + <warning descr="Ellipsis with ... instead of \ldots or \dots">...</warning>
                  \end{equation}
                \end{document}
            """.trimIndent()
        )
        myFixture.checkHighlighting()
    }

    fun testNoInspections() {
        myFixture.configureByText(
            "main.tex",
            """
                \documentclass{article}
                \usepackage{amsmath}
                \usepackage{tikz}
                \begin{document}
                  \begin{tikzpicture}
                    ...
                  \end{tikzpicture}
                  \begin{equation}
                    \label{eq:...}
                  \end{equation}
                \end{document}
            """.trimIndent()
        )
        myFixture.updateCommandDef()
        myFixture.checkHighlighting()
    }
}