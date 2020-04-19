package nl.hannahsten.texifyidea.inspections.latex

import nl.hannahsten.texifyidea.file.LatexFileType
import nl.hannahsten.texifyidea.inspections.TexifyInspectionTestBase

internal class LatexUnescapedSpecialCharacterInspectionTest : TexifyInspectionTestBase(LatexUnescapedSpecialCharacterInspection()) {

    fun `test unescaped special character warning`() {
        myFixture.configureByText(LatexFileType, """
            \begin{document}
                some text <warning descr="Escape character \ expected">_</warning> with unescaped special character
            \end{document}
        """.trimIndent())
        myFixture.checkHighlighting(true, false, false, false)
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

    fun `test unescaped special character quick fix for _`() {
        testQuickFix("""
            \begin{document}
                _ some text with unescaped special character
            \end{document}
        """.trimIndent(), """
            \begin{document}
                \_ some text with unescaped special character
            \end{document}
        """.trimIndent())
    }
}
