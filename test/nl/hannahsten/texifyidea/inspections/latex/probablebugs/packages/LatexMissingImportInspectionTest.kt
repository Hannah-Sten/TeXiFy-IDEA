package nl.hannahsten.texifyidea.inspections.latex.probablebugs.packages

import com.intellij.testFramework.fixtures.impl.CodeInsightTestFixtureImpl
import io.mockk.every
import io.mockk.mockkStatic
import nl.hannahsten.texifyidea.file.LatexFileType
import nl.hannahsten.texifyidea.inspections.TexifyInspectionTestBase
import nl.hannahsten.texifyidea.testutils.writeCommand
import nl.hannahsten.texifyidea.updateCommandDef
import nl.hannahsten.texifyidea.util.runCommandWithExitCode

class LatexMissingImportInspectionTest : TexifyInspectionTestBase(LatexMissingImportInspection()) {

    override fun getTestDataPath(): String {
        return "test/resources/inspections/latex/missingimport"
    }

    override fun setUp() {
        super.setUp()
        myFixture.copyDirectoryToProject("", "")
        (myFixture as CodeInsightTestFixtureImpl).canChangeDocumentDuringHighlighting(true)
        mockkStatic(::runCommandWithExitCode)
        every { runCommandWithExitCode(*anyVararg(), workingDirectory = any(), timeout = any(), returnExceptionMessage = any()) } returns Pair(null, 0)
    }

    fun testWarning() {
        myFixture.configureByText(
            LatexFileType,
            """
            <error descr="Command requires any of the packages: xcolor, color">\color</error>{blue}
            """.trimIndent()

        )
        myFixture.updateCommandDef()
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
        myFixture.updateCommandDef()
        val quickFixes = myFixture.getAllQuickFixes()
        assertEquals(2, quickFixes.size)
        writeCommand(myFixture.project) {
            quickFixes.first { it.text.contains("xcolor") }.invoke(myFixture.project, myFixture.editor, myFixture.file)
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
        myFixture.updateCommandDef()
        myFixture.checkHighlighting()
    }

    fun `test package not imported in subfile root`() {
        myFixture.configureByFiles("missingsub.tex", "missingmain.tex")
        myFixture.updateCommandDef()
        myFixture.checkHighlighting()
    }

    fun testNestedImport() {
        myFixture.configureByText(
            "mypackage.sty",
            """
            \ProvidesPackage{mypackage}
            \RequirePackage{xcolor}
            """.trimIndent()
        )
        myFixture.configureByText(
            "main.tex",
            """
            \documentclass{article}
            \usepackage{mypackage}
            \begin{document}
                \color{blue}
            \end{document}
            """.trimIndent(),
        )
        myFixture.updateCommandDef()
        myFixture.checkHighlighting()
    }

    fun testNestedImportWithSubfile() {
        myFixture.configureByText(
            "mypackage.sty",
            """
            \ProvidesPackage{mypackage}
            \RequirePackage{xcolor}
            """.trimIndent()
        )
        myFixture.configureByText(
            "main.tex",
            """
            \documentclass{article}
            \usepackage{mypackage}
            \usepackage{subfiles}
            \begin{document}
                \input{sub1/one.tex}
            \end{document}
            """.trimIndent(),
        )
        myFixture.configureByFiles("sub1/sub2/two.tex", "sub1/one.tex")
        myFixture.updateCommandDef()
        myFixture.checkHighlighting()
    }
}