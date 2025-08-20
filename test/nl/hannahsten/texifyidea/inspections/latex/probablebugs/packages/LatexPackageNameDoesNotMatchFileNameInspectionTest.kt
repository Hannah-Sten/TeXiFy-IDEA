package nl.hannahsten.texifyidea.inspections.latex.probablebugs.packages

import com.intellij.testFramework.fixtures.BasePlatformTestCase
import nl.hannahsten.texifyidea.testutils.writeCommand
import nl.hannahsten.texifyidea.updateFilesets

class LatexPackageNameDoesNotMatchFileNameInspectionTest : BasePlatformTestCase() {

    override fun getTestDataPath(): String {
        return "test/resources/inspections/latex/packagenamedoesnotmatch"
    }

    override fun setUp() {
        super.setUp()
        myFixture.enableInspections(
            LatexPackageNameDoesNotMatchFileNameInspection(),
            LatexPackageSubdirectoryInspection()
        )
    }

    fun testWarnings() {
        myFixture.configureByFile("mypackage_warnings.sty")
        myFixture.checkHighlighting()
    }

    fun testNoWarning() {
        myFixture.configureByFile("mypackge.sty")
        myFixture.checkHighlighting()
    }

    fun testQuickfix() {
        myFixture.configureByFile("mypackage.sty")
        val quickFixes = myFixture.getAllQuickFixes()
        assertEquals(1, quickFixes.size)
        writeCommand(myFixture.project) {
            quickFixes.first().invoke(myFixture.project, myFixture.editor, myFixture.file)
        }

        myFixture.checkResult(
            """
            \NeedsTeXFormat{LaTeX2e}
            \ProvidesPackage{mypackage}[My Package]
            """.trimIndent()
        )
    }

    fun testAddingDirectory() {
        myFixture.configureByFiles("pkg/mypackagequickfix.sty", "main.tex")
        myFixture.updateFilesets()
        val quickFixes = myFixture.getAllQuickFixes()
        assertEquals(1, quickFixes.size)
        writeCommand(myFixture.project) {
            quickFixes.first().invoke(myFixture.project, myFixture.editor, myFixture.file)
        }

        myFixture.checkResult(
            """
            \NeedsTeXFormat{LaTeX2e}
            \ProvidesPackage{pkg/mypackagequickfix}[My Package]
            """.trimIndent()
        )
    }

    fun testNoWarnings() {
        myFixture.configureByFiles("pkg/secondpackage.sty", "main.tex")
        myFixture.updateFilesets()
        myFixture.checkHighlighting()
    }

    fun testSubdirWarnings() {
        myFixture.configureByFiles("pkg/mypackage.sty", "main.tex")
        myFixture.updateFilesets()
        myFixture.checkHighlighting()
    }
}