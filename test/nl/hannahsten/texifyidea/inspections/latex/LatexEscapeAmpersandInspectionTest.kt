package nl.hannahsten.texifyidea.inspections.latex

import nl.hannahsten.texifyidea.file.LatexFileType
import nl.hannahsten.texifyidea.inspections.TexifyInspectionTestBase

internal class LatexEscapeAmpersandInspectionTest : TexifyInspectionTestBase(LatexEscapeAmpersandInspection()) {

    fun `test unescaped special character warning`() {
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

    fun `test unescaped special character quick fix for &`() {
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
