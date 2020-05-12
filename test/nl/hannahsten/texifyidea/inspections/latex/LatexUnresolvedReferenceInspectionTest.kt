package nl.hannahsten.texifyidea.inspections.latex

import nl.hannahsten.texifyidea.file.LatexFileType
import nl.hannahsten.texifyidea.inspections.TexifyInspectionTestBase

class LatexUnresolvedReferenceInspectionTest : TexifyInspectionTestBase(LatexUnresolvedReferenceInspection()) {

    override fun getTestDataPath(): String {
        return "test/resources/inspections/latex/unresolvedreference"
    }

    fun testWarning() {
        myFixture.configureByText(LatexFileType, """
            \ref{<warning descr="Unresolved reference 'alsonot'">alsonot</warning>}
            \cite{<warning descr="Unresolved reference 'nope'">nope</warning>}
        """.trimIndent())
        myFixture.checkHighlighting()
    }

    fun testNoWarning() {
        myFixture.configureByText(LatexFileType, """
            \label{alabel}
            \ref{alabel}
        """.trimIndent())
        myFixture.checkHighlighting()
    }

    fun testBibtexReference() {
        myFixture.configureByFiles(getTestName(false) + ".tex", "references.bib")
        myFixture.checkHighlighting()
    }
}