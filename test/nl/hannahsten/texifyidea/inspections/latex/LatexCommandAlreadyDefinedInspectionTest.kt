package nl.hannahsten.texifyidea.inspections.latex

import nl.hannahsten.texifyidea.file.LatexFileType
import nl.hannahsten.texifyidea.inspections.TexifyInspectionTestBase

class LatexCommandAlreadyDefinedInspectionTest : TexifyInspectionTestBase(LatexCommandAlreadyDefinedInspection()) {
    fun testWarning() {
        myFixture.configureByText(LatexFileType, """
            <error descr="Command is already defined">\newcommand{\cite}{\citeauthor}</error>
            
            <warning descr="Command is already defined">\def</warning>\citeauthor\cite
            
            \newcommand{\notexists}{}
        """.trimIndent())
        myFixture.checkHighlighting()
    }
}