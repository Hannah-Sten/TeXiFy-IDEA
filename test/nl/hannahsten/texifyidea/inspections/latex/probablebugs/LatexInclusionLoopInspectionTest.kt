package nl.hannahsten.texifyidea.inspections.latex.probablebugs

import nl.hannahsten.texifyidea.inspections.TexifyInspectionTestBase
import nl.hannahsten.texifyidea.updateFilesets

class LatexInclusionLoopInspectionTest : TexifyInspectionTestBase(LatexInclusionLoopInspection()) {

    override fun getTestDataPath(): String {
        return "test/resources/inspections/latex/inclusionloop"
    }

    fun testWarning() {
        myFixture.configureByFiles("included.tex", "main.tex")
        myFixture.updateFilesets()
        myFixture.checkHighlighting()
    }

    fun testIncludingPackage() {
        myFixture.configureByFiles("main.tex", "main.sty")
        myFixture.checkHighlighting()
    }

    fun testInspectionIsExtensionAware() {
        myFixture.configureByFiles("extensioninclusion.tex")
        myFixture.checkHighlighting()
    }

    fun testDocumentclass() {
        myFixture.configureByText(
            "book.tex", """
            \documentclass{book}
        """.trimIndent()
        )
        myFixture.checkHighlighting()
    }

    fun testIncludeGraphics() {
        myFixture.configureByText(
            "lab.tex", """
            \includegraphics{lab}  % lab.pdf
        """.trimIndent()
        )
        myFixture.checkHighlighting()
    }

}