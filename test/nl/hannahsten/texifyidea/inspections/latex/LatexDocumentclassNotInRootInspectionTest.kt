package nl.hannahsten.texifyidea.inspections.latex

import nl.hannahsten.texifyidea.file.LatexFileType
import nl.hannahsten.texifyidea.inspections.TexifyInspectionTestBase
import nl.hannahsten.texifyidea.inspections.latex.codestyle.LatexDocumentclassNotInRootInspection

class LatexDocumentclassNotInRootInspectionTest : TexifyInspectionTestBase(LatexDocumentclassNotInRootInspection()) {

    override fun getTestDataPath(): String {
        return "test/resources/inspections/latex/documentclassnotinroot"
    }

    fun testNoWarning() {
        myFixture.configureByText(
                LatexFileType,
                """
                    \documentclass{article}
                    
                    \begin{document}
                        bla
                    \end{document}
            """.trimIndent()
        )
        myFixture.checkHighlighting()
    }

    fun `test no warning with environment in preamble`() {
        myFixture.configureByText(LatexFileType,
        """
            \documentclass{article}
            
            \begin{filecontents}{a}
                bla
            \end
            
            \begin{document}
                contents
            \end{document}
        """.trimIndent())
        myFixture.checkHighlighting()
    }

    fun testWarning() {
        myFixture.configureByFiles("preamble.sty", "main.tex")
        myFixture.checkHighlighting()
    }
}