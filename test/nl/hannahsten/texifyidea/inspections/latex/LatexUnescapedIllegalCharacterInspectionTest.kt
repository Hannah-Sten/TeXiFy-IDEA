package nl.hannahsten.texifyidea.inspections.latex

import nl.hannahsten.texifyidea.file.LatexFileType
import nl.hannahsten.texifyidea.inspections.TexifyInspectionTestBase

internal class LatexUnescapedIllegalCharacterInspectionTest : TexifyInspectionTestBase(LatexUnescapedIllegalCharacterInspection()) {

    fun `test unescaped special character warning`() {
        myFixture.configureByText(LatexFileType, """
            \begin{document}
                some text <warning descr="Special characters need to be escaped">#</warning> with unescaped special character
            \end{document}
        """.trimIndent())
        myFixture.checkHighlighting(true, false, false, false)
    }

    fun `test unescaped special character quick fix for #`() {
        testQuickFix("""
            \begin{document}
                #hashtag
            \end{document}
        """.trimIndent(), """
            \begin{document}
                \#hashtag
            \end{document}
        """.trimIndent())
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
                some text _ with unescaped special character
            \end{document}
        """.trimIndent(), """
            \begin{document}
                some text \_ with unescaped special character
            \end{document}
        """.trimIndent())
    }

}