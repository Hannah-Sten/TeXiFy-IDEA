package nl.hannahsten.texifyidea.inspections.latex.codestyle

import io.mockk.every
import io.mockk.mockkStatic
import nl.hannahsten.texifyidea.inspections.TexifyInspectionTestBase
import nl.hannahsten.texifyidea.util.runCommandWithExitCode

class LatexTooLargeSectionInspectionTest : TexifyInspectionTestBase(LatexTooLargeSectionInspection()) {

    override fun setUp() {
        super.setUp()
        mockkStatic(::runCommandWithExitCode)
        every { runCommandWithExitCode(*anyVararg(), workingDirectory = any(), timeout = any(), returnExceptionMessage = any()) } returns Pair(null, 0)
    }

    override fun getTestDataPath(): String = "test/resources/inspections/latex/toolargesection"

    fun `test too large section`() {
        myFixture.configureByFiles("large.tex")
        myFixture.checkHighlighting()
    }

    fun `test small enough section`() {
        myFixture.configureByFiles("small.tex")
        myFixture.checkHighlighting()
    }

    fun `test single section`() {
        myFixture.configureByFiles("single.tex")
        myFixture.checkHighlighting()
    }

    fun `test file with large subsection`() {
        myFixture.configureByFiles("subsection.tex")
        myFixture.checkHighlighting()
    }

    fun `test section ended by new chapter`() {
        myFixture.configureByFiles("endedbychapter.tex")
        myFixture.checkHighlighting()
    }

    fun `test long single chapter`() {
        myFixture.configureByFiles("longsinglechapter.tex")
        myFixture.checkHighlighting()
    }
}
