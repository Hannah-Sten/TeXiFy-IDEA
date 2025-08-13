package nl.hannahsten.texifyidea.inspections.latex.probablebugs

import nl.hannahsten.texifyidea.file.LatexFileType
import nl.hannahsten.texifyidea.inspections.TexifyInspectionTestBase
import nl.hannahsten.texifyidea.inspections.latex.probablebugs.packages.LatexUndefinedCommandInspection
import nl.hannahsten.texifyidea.updateCommandDef

class LatexUndefinedCommandInspectionTest : TexifyInspectionTestBase(LatexUndefinedCommandInspection()) {

    fun testUndefinedCommand() {
        myFixture.configureByText(LatexFileType, """<error descr="Undefined command: \blub">\blub</error>""")
        myFixture.updateCommandDef()
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
        myFixture.updateCommandDef()
        myFixture.checkHighlighting()
    }
}