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
        testInsertMissingParentCommandQuickFix(
            """
            \begin{document}
                \section{}
                \subsubsection{}
            \end{document}
            """.trimIndent(),
            """
            \begin{document}
                \section{}
                \subsection{<caret>}
                \subsubsection{}
            \end{document}
            """.trimIndent()
        )
    }

    @Test
    fun `test subsection after chapter warning`() {
        testInsertMissingParentCommandQuickFix(
            """
            \begin{document}
                \chapter{}
                \subsection{}
            \end{document}
            """.trimIndent(),
            """
            \begin{document}
                \chapter{}
                \section{<caret>}
                \subsection{}
            \end{document}
            """.trimIndent()
        )
    }

    @Test
    fun `test change subsubsection to subsection quick fix`() {
        testChangeToParentCommandQuickFix(
            """
            \begin{document}
                \section{}
                \subsubsection{}
            \end{document}
            """.trimIndent(),
            """
            \begin{document}
                \section{}
                \subsection{}
            \end{document}
            """.trimIndent()
        )
    }

    @Test
    fun `test subparagraph after section warning`() {
        testInsertMissingParentCommandQuickFix(
            """
            \begin{document}
                \section{}
                \subparagraph{}
            \end{document}
            """.trimIndent(),
            """
            \begin{document}
                \section{}
                \paragraph{<caret>}
                \subparagraph{}
            \end{document}
            """.trimIndent()
        )
    }

    @Test
    fun `test missing parent command warning`() {
        myFixture.configureByText(
            LatexFileType,
            """
             \begin{document}
                \section{}
                <weak_warning descr="Incorrect nesting">\subsubsection{}</weak_warning>
             \end{document}
            """.trimIndent()
        )
        myFixture.checkHighlighting(false, false, true, false)
    }

    @Test
    fun `test no warning on correct nesting`() {
        myFixture.configureByText(
            LatexFileType,
            """
             \begin{document}
                \part{}
                \part{}
                \chapter{}
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
            """.trimIndent()
        )
        myFixture.checkHighlighting(false, false, true, false)
    }

    private fun testInsertMissingParentCommandQuickFix(before: String, after: String) {
        myFixture.configureByText(LatexFileType, before)
        val quickFixes = myFixture.getAllQuickFixes()
        writeCommand(myFixture.project) {
            quickFixes.first { it.familyName == "Insert missing parent command" }.invoke(myFixture.project, myFixture.editor, myFixture.file)
        }

        myFixture.checkResult(after)
    }

    private fun testChangeToParentCommandQuickFix(before: String, after: String) {
        myFixture.configureByText(LatexFileType, before)
        val quickFixes = myFixture.getAllQuickFixes()
        writeCommand(myFixture.project) {
            quickFixes.first { it.familyName == "Change to parent command" }.invoke(myFixture.project, myFixture.editor, myFixture.file)
        }

        myFixture.checkResult(after)
    }
}