package nl.hannahsten.texifyidea.inspections.latex.codematurity

import nl.hannahsten.texifyidea.file.LatexFileType
import nl.hannahsten.texifyidea.inspections.TexifyInspectionTestBase

class LatexPrimitiveEquationInspectionTest : TexifyInspectionTestBase(LatexPrimitiveEquationInspection()) {

    fun testNoWarning() {
        myFixture.configureByText(
            LatexFileType,
            """
            doughnuts for \$$8.45$ and \$$6.30$.
            """.trimIndent()
        )
        myFixture.checkHighlighting()
    }
}