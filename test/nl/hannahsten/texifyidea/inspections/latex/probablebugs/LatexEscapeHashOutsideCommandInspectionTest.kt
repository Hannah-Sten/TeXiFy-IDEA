package nl.hannahsten.texifyidea.inspections.latex.probablebugs

import nl.hannahsten.texifyidea.file.LatexFileType
import nl.hannahsten.texifyidea.inspections.TexifyInspectionTestBase

internal class LatexEscapeHashOutsideCommandInspectionTest : TexifyInspectionTestBase(LatexEscapeHashOutsideCommandInspection()) {

    fun `test that unescaped hash in text triggers a warning`() {
        myFixture.configureByText(
            LatexFileType,
            """
            \begin{document}
                Dummy text <weak_warning descr="unescaped #">#</weak_warning>.
            \end{document}
            """.trimIndent()
        )
        myFixture.checkHighlighting(false, false, true, false)
    }

    fun `test that unescaped hash in new command does not trigger a warning`() {
        myFixture.configureByText(
            LatexFileType,
            """
            \newcommand{\mycommand}[1]{#1}
            """.trimIndent()
        )
        myFixture.checkHighlighting(false, false, true, false)
    }

    fun `test that unescaped hash in new environment does not trigger a warning`() {
        myFixture.configureByText(
            LatexFileType,
            """
            \newenvironment{\myenvironment}[2]{#1}{#2}
            """.trimIndent()
        )
        myFixture.checkHighlighting(false, false, true, false)
    }

    fun `test that an unescaped hash is escaped by its quick fix`() {
        testQuickFix(
            """
            \begin{document}
                Lorem # ipsum.
            \end{document}
            """.trimIndent(),
            """
            \begin{document}
                Lorem \# ipsum.
            \end{document}
            """.trimIndent()
        )
    }
}
