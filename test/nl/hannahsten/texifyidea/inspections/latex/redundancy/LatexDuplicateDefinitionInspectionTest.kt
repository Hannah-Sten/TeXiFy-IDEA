package nl.hannahsten.texifyidea.inspections.latex.redundancy

import nl.hannahsten.texifyidea.file.LatexFileType
import nl.hannahsten.texifyidea.inspections.TexifyInspectionTestBase

class LatexDuplicateDefinitionInspectionTest : TexifyInspectionTestBase(LatexDuplicateDefinitionInspection()) {

    fun testWarning() {
        myFixture.configureByText(
            LatexFileType,
            """
            <error descr="Command '\cmdtwo' is defined multiple times">\newcommand{\cmdtwo}{a}</error>
            <error descr="Command '\cmdtwo' is defined multiple times">\newcommand{\cmdtwo}{a}</error>
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

    fun testNewcommand() {
        myFixture.configureByText(
            LatexFileType,
            """
            \newcommand{\foo}{}
            \renewcommand{\foo}{}
            """.trimIndent()
        )
        myFixture.checkHighlighting()
    }
}