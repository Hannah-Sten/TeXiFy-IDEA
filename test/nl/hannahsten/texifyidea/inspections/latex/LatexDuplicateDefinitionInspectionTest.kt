package nl.hannahsten.texifyidea.inspections.latex

import nl.hannahsten.texifyidea.file.LatexFileType
import nl.hannahsten.texifyidea.inspections.TexifyInspectionTestBase
import nl.hannahsten.texifyidea.inspections.latex.duplicates.LatexDuplicateDefinitionInspection

class LatexDuplicateDefinitionInspectionTest : TexifyInspectionTestBase(LatexDuplicateDefinitionInspection()) {

    fun testWarning() {
        myFixture.configureByText(
            LatexFileType,
            """
            <error descr="Command '\cmdtwo' is defined multiple times">\newcommand{\cmdtwo}{a}</error>
            <error descr="Command '\cmdtwo' is defined multiple times">\newcommand{\cmdtwo}{a}</error>
            
            \providecommand{\test}{}
            <error descr="Command '\test' is defined multiple times">\newcommand{\test}{}</error>
            """.trimIndent()
        )
        myFixture.checkHighlighting()
    }

    fun testNoWarning() {
        myFixture.configureByText(
            LatexFileType,
            """
            \providecommand{\cmd}{a}
            \renewcommand{\cmd}{a}
            
            \providecommand{\provided}{}
            """.trimIndent()
        )
        myFixture.checkHighlighting()
    }
}