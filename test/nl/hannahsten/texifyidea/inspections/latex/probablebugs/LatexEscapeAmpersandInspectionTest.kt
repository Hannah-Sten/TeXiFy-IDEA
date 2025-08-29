package nl.hannahsten.texifyidea.inspections.latex.probablebugs

import nl.hannahsten.texifyidea.file.LatexFileType
import nl.hannahsten.texifyidea.inspections.TexifyInspectionTestBase
import nl.hannahsten.texifyidea.updateCommandDef
import nl.hannahsten.texifyidea.util.magic.EnvironmentMagic

internal class LatexEscapeAmpersandInspectionTest : TexifyInspectionTestBase(LatexEscapeAmpersandInspection()) {

    fun `test unescaped & character warning`() {
        myFixture.configureByText(
            LatexFileType,
            """
            \begin{document}
                some text <warning descr="Escape character \ expected">&</warning> with unescaped special character
            \end{document}
            """.trimIndent()
        )
        myFixture.checkHighlighting(true, false, false, false)
    }

    fun `test that ampersand in tabular environment does not trigger a warning`() {
        myFixture.configureByText(
            LatexFileType,
            """
            \begin{document}
                \begin{tabular}{ll}
                    1 & 2 \\
                \end{tabular}
            \end{document}
            """.trimIndent()
        )
        myFixture.checkHighlighting(true, false, false, false)
    }

    fun `test that ampersand in custom tabular environment does not trigger a warning`() {
        myFixture.configureByText(
            LatexFileType,
            """
            \newenvironment{kwak}{ \begin{tabular}{lc} }{ \end{tabular} }
            \newcommand{\kwek}[1]{ \begin{kwak} #1 \end{kwak} }
            \begin{document}
                \begin{kwak}
                    kwik & kwek \\
                \end{kwak}
                \kwek{kwak & kwek}
            \end{document}
            """.trimIndent()
        )
        myFixture.updateCommandDef()
        myFixture.checkHighlighting(true, false, false, false)
    }

    fun `test that ampersand in math environment does trigger a warning`() {
        myFixture.configureByText(
            LatexFileType,
            """
            \begin{document}
                $<warning descr="Escape character \ expected">&</warning>$
            \end{document}
            """.trimIndent()
        )
        myFixture.checkHighlighting(true, false, false, false)
    }

    fun `test that ampersand in split env in math environment does not trigger a warning`() {
        myFixture.configureByText(
            LatexFileType,
            """
            \usepackage{amsmath}
            \begin{document}
            
                $\begin{split}
                    a& =b\\
                    c& =d
                \end{split}$
            \end{document}
            """.trimIndent()
        )
        myFixture.updateCommandDef()
        myFixture.checkHighlighting(true, false, false, false)
    }

    fun `test that ampersand in matrix environments does not trigger a warning`() {
        EnvironmentMagic.matrixEnvironments.forEach { environment ->
            myFixture.configureByText(
                LatexFileType,
                """
                \usepackage{amsmath,mathtools}
                \usepackage{gauss}
                \usepackage{tikzcd}
                \begin{document}
                    $\begin{$environment}
                        a& =b\\
                        c& =d
                    \end{$environment}$
                \end{document}
                """.trimIndent()
            )
            myFixture.updateCommandDef()
            myFixture.checkHighlighting(true, false, false, false)
        }
    }

    fun `test that ampersand in url command does not trigger a warning`() {
        myFixture.configureByText(
            LatexFileType,
            """
            \url{a&b}
            """.trimIndent()
        )
        myFixture.checkHighlighting(true, false, false, false)
    }

    fun `test unescaped & character quick fix`() {
        testQuickFix(
            """
            \begin{document}
                H&M
            \end{document}
            """.trimIndent(),
            """
            \begin{document}
                H\&M
            \end{document}
            """.trimIndent()
        )
    }
}
