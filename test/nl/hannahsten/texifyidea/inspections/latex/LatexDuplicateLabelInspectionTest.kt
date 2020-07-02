package nl.hannahsten.texifyidea.inspections.latex

import nl.hannahsten.texifyidea.file.LatexFileType
import nl.hannahsten.texifyidea.inspections.TexifyInspectionTestBase

class LatexDuplicateLabelInspectionTest : TexifyInspectionTestBase(LatexDuplicateLabelInspection()) {
    fun testWarning() {
        myFixture.configureByText(LatexFileType, """
            \label{<error descr="Duplicate label 'some-label'">some-label</error>}
            \label{<error descr="Duplicate label 'some-label'">some-label</error>}
        """.trimIndent())
        myFixture.checkHighlighting()
    }
}