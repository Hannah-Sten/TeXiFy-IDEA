package nl.hannahsten.texifyidea.inspections.latex.probablebugs.packages

import com.intellij.testFramework.fixtures.impl.CodeInsightTestFixtureImpl
import nl.hannahsten.texifyidea.file.LatexFileType
import nl.hannahsten.texifyidea.inspections.TexifyInspectionTestBase
import nl.hannahsten.texifyidea.testutils.writeCommand

class LatexMissingImportInspectionTest : TexifyInspectionTestBase(LatexMissingImportInspection()) {

    override fun getTestDataPath(): String {
        return "test/resources/inspections/latex/missingimport"
    }

    override fun setUp() {
        super.setUp()
        myFixture.copyDirectoryToProject("", "")
        (myFixture as CodeInsightTestFixtureImpl).canChangeDocumentDuringHighlighting(true)
    }

    fun testWarning() {
        myFixture.configureByText(
            LatexFileType,
            """
            <error descr="Command requires color, or xcolor package">\color</error>{blue}
            """.trimIndent()
        )
        myFixture.checkHighlighting()
    }

    fun testQuickfix() {
        myFixture.configureByText(
            LatexFileType,
            """
            \documentclass{article}

            \usepackage{amsmath}

            \begin{document}
                \color{blue}
            \end{document}
            """.trimIndent()
        )

        val quickFixes = myFixture.getAllQuickFixes()
        assertEquals(2, quickFixes.size)
        writeCommand(myFixture.project) {
            quickFixes.last().invoke(myFixture.project, myFixture.editor, myFixture.file)
        }

        myFixture.checkResult(
            """
            \documentclass{article}

            \usepackage{amsmath}
            \usepackage{xcolor}

            \begin{document}
                \color{blue}
            \end{document}
            """.trimIndent()
        )
    }

    fun `test package imported in subfile root`() {
        myFixture.configureByFiles("main.tex", "sub.tex")
        myFixture.checkHighlighting()
    }

    fun `test package not imported in subfile root`() {
        myFixture.configureByFiles("missingsub.tex", "missingmain.tex")
        myFixture.checkHighlighting()
    }
}