package nl.hannahsten.texifyidea.inspections.latex

import nl.hannahsten.texifyidea.file.LatexFileType
import nl.hannahsten.texifyidea.inspections.TexifyInspectionTestBase
import nl.hannahsten.texifyidea.util.Magic

internal class LatexEscapeAmpersandInspectionTest : TexifyInspectionTestBase(LatexEscapeAmpersandInspection()) {

    fun `test unescaped & character warning`() {
        myFixture.configureByText(LatexFileType, """
            \begin{document}
                some text <warning descr="Escape character \ expected">&</warning> with unescaped special character
            \end{document}
        """.trimIndent())
        myFixture.checkHighlighting(true, false, false, false)
    }

    fun `test that ampersand in tabular environment does not trigger a warning`() {
        myFixture.configureByText(LatexFileType, """
            \begin{document}
                \begin{tabular}{ll}
                    1 & 2 \\
                \end{tabular}
            \end{document}
        """.trimIndent())
        myFixture.checkHighlighting(false, false, false, false)
    }

    fun `test that ampersand in math environment does trigger a warning`() {
        myFixture.configureByText(LatexFileType, """
            \begin{document}
                $<warning descr="Escape character \ expected">&</warning>$
            \end{document}
        """.trimIndent())
        myFixture.checkHighlighting(true, false, false, false)
    }

    fun `test that ampersand in split env in math environment does not trigger a warning`() {
        myFixture.configureByText(LatexFileType, """
            \begin{document}
                $\begin{split}
                    a& =b\\
                    c& =d
                \end{split}$
            \end{document}
        """.trimIndent())
        myFixture.checkHighlighting(true, false, false, false)
    }

    fun `test that ampersand in in matrix environments does not trigger a warning`() {
        Magic.Environment.matrixEnvironments.forEach { environment ->
            myFixture.configureByText(LatexFileType, """
                \begin{document}
                    $\begin{$environment}
                        a& =b\\
                        c& =d
                    \end{$environment}$
                \end{document}
            """.trimIndent())
            myFixture.checkHighlighting(true, false, false, false)
        }
    }

    fun `test that ampersand in url command does not trigger a warning`() {
        myFixture.configureByText(LatexFileType, """
            \url{a&b}
        """.trimIndent())
        myFixture.checkHighlighting(true, false, false, false)
    }

    fun `test unescaped & character quick fix`() {
        testQuickFix("""
            \begin{document}
                H&M
            \end{document}
        """.trimIndent(), """
            \begin{document}
                H\&M
            \end{document}
        """.trimIndent())
    }
}
