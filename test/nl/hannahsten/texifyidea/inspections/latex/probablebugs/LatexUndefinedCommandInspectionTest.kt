package nl.hannahsten.texifyidea.inspections.latex.probablebugs

import nl.hannahsten.texifyidea.file.LatexFileType
import nl.hannahsten.texifyidea.inspections.TexifyInspectionTestBase

class LatexUndefinedCommandInspectionTest : TexifyInspectionTestBase(LatexUndefinedCommandInspection()) {

    fun testUndefinedCommand() {
        myFixture.configureByText(LatexFileType, """\blub""")
        myFixture.checkHighlighting()
    }

    fun testDefinedCommand() {
        myFixture.configureByText(LatexFileType, """
            \documentclass{article}
            \newcommand{\floep}{\grq}
            \floep
        """.trimIndent())
        myFixture.checkHighlighting()
    }

}