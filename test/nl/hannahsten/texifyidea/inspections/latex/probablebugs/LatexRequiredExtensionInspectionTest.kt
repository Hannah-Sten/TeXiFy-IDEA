package nl.hannahsten.texifyidea.inspections.latex.probablebugs

import nl.hannahsten.texifyidea.file.LatexFileType
import nl.hannahsten.texifyidea.inspections.TexifyInspectionTestBase
import nl.hannahsten.texifyidea.testutils.writeCommand
import nl.hannahsten.texifyidea.updateCommandDef

class LatexRequiredExtensionInspectionTest : TexifyInspectionTestBase(LatexRequiredExtensionInspection()) {

    override fun getTestDataPath(): String = "test/resources/inspections/latex/requiredextension"

    fun testWarning() {
        myFixture.configureByText(
            LatexFileType,
            """
            \usepackage{biblatex}
            \addbibresource{<error descr="File argument should include the extension">test</error>}
            """.trimIndent()
        )
        myFixture.updateCommandDef()
        myFixture.checkHighlighting()
    }

    fun testNoWarning() {
        myFixture.configureByText(
            LatexFileType,
            """
            \addbibresource{test.bib}
            """.trimIndent()
        )
        myFixture.checkHighlighting()
    }

    fun testNoWarningCitationStyleLanguage() {
        myFixture.configureByText(
            LatexFileType,
            """
            \usepackage{citation-style-language}
            \addbibresource{test.json}
            """.trimIndent()
        )
        myFixture.checkHighlighting()
    }

    fun testQuickfix() {
        myFixture.configureByFiles("main-broken.tex", "main.bib")
        myFixture.updateCommandDef()

        val quickFixes = myFixture.getAllQuickFixes()
        assertEquals(1, quickFixes.size)
        writeCommand(myFixture.project) {
            quickFixes.first().invoke(myFixture.project, myFixture.editor, myFixture.file)
        }

        myFixture.checkResultByFile("main-fixed.tex")
    }

    fun testQuickfixCitationStyleLanguageJson() {
        myFixture.configureByFiles("csl-main-broken.tex", "csl-main.json")
        myFixture.updateCommandDef()

        val quickFixes = myFixture.getAllQuickFixes()
        assertEquals(1, quickFixes.size)
        writeCommand(myFixture.project) {
            quickFixes.first().invoke(myFixture.project, myFixture.editor, myFixture.file)
        }

        myFixture.checkResultByFile("csl-main-fixed.tex")
    }
}