package nl.hannahsten.texifyidea.inspections.latex

import com.intellij.testFramework.fixtures.BasePlatformTestCase
import nl.hannahsten.texifyidea.file.LatexFileType
import nl.hannahsten.texifyidea.testutils.writeCommand
import org.junit.Test

internal class LatexIncorrectSectionNestingInspectionTest : BasePlatformTestCase() {

    override fun setUp() {
        super.setUp()
        myFixture.enableInspections(LatexIncorrectSectionNestingInspection())
    }

    @Test
    fun `test document missing subsection warning`() {
        testQuickFix("""
            \begin{document}
                \section{}
                \subsubsection{}
            \end{document}
        """.trimIndent(), """
            \begin{document}
                \section{}
                \subsection{<caret>}
                \subsubsection{}
            \end{document}
        """.trimIndent())
    }

    @Test
    fun `test document starting with subsection warning`() {
        testQuickFix("""
            \begin{document}
                \subsection{}
            \end{document}
        """.trimIndent(), """
            \begin{document}
                \section{<caret>}
                \subsection{}
            \end{document}
        """.trimIndent())
    }

    @Test
    fun `test document starting with subparagraph warning`() {
        testQuickFix("""
            \begin{document}
                \subparagraph{}
            \end{document}
        """.trimIndent(), """
            \begin{document}
                \paragraph{<caret>}
                \subparagraph{}
            \end{document}
        """.trimIndent())
    }

    @Test
    fun `test subparagraph after section warning`() {
        testQuickFix("""
            \begin{document}
                \section{}
                \subparagraph{}
            \end{document}
        """.trimIndent(), """
            \begin{document}
                \section{}
                \paragraph{<caret>}
                \subparagraph{}
            \end{document}
        """.trimIndent())
    }

    @Test
    fun `test missing parent command warning`() {
        myFixture.configureByText(LatexFileType, """
             \begin{document}
                \section{}
                <warning descr="Incorrect nesting">\subsubsection{}</warning>
             \end{document}
         """.trimIndent())
        myFixture.checkHighlighting(true, false, false, false)
    }

    @Test
    fun `test no warning on correct nesting`() {
        myFixture.configureByText(LatexFileType, """
             \begin{document}
                \section{}
                \section{}
                \subsection{}
                \subsubsection{}
                \subsubsection{}
                \section{}
                \paragraph{}
                \paragraph{}
                \subparagraph{}
                \subparagraph{}
             \end{document}
         """.trimIndent())
        myFixture.checkHighlighting(true, false, false, false)
    }

    private fun testQuickFix(before: String, after: String) {
        myFixture.configureByText(LatexFileType, before)
        val quickFixes = myFixture.getAllQuickFixes()
        writeCommand(myFixture.project) {
            quickFixes.first().invoke(myFixture.project, myFixture.editor, myFixture.file)
        }

        myFixture.checkResult(after)
    }
}