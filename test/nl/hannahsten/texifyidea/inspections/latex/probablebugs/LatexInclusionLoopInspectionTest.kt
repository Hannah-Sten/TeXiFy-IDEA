package nl.hannahsten.texifyidea.inspections.latex.probablebugs

import nl.hannahsten.texifyidea.inspections.TexifyInspectionTestBase

class LatexInclusionLoopInspectionTest : TexifyInspectionTestBase(LatexInclusionLoopInspection()) {

    override fun getTestDataPath(): String {
        return "test/resources/inspections/latex/inclusionloop"
    }

    // Test is not working
    // fun testWarning() {
    //     myFixture.configureByFiles("included.tex", "main.tex")
    //     myFixture.checkHighlighting()
    // }

    fun testIncludingPackage() {
        myFixture.configureByFiles("main.tex", "main.sty")
        myFixture.checkHighlighting()
    }

    fun testInspectionIsExtensionAware() {
        myFixture.configureByFiles("extensioninclusion.tex")
        myFixture.checkHighlighting()
    }
}