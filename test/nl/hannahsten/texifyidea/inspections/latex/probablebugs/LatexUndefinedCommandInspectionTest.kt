package nl.hannahsten.texifyidea.inspections.latex.probablebugs

import nl.hannahsten.texifyidea.file.LatexFileType
import nl.hannahsten.texifyidea.inspections.TexifyInspectionTestBase
import nl.hannahsten.texifyidea.inspections.latex.probablebugs.packages.LatexUndefinedCommandInspection

class LatexUndefinedCommandInspectionTest : TexifyInspectionTestBase(LatexUndefinedCommandInspection()) {

    fun testUndefinedCommand() {
        myFixture.configureByText(LatexFileType, """<error descr="Command \blub is not defined">\blub</error>""")
        myFixture.checkHighlighting()
    }

    fun testDefinedCommand() {
        myFixture.configureByText(
            LatexFileType,
            """
            \documentclass{article}
            \newcommand{\floep}{\grq}
            \floep
            """.trimIndent()
        )
        myFixture.checkHighlighting()
    }
}