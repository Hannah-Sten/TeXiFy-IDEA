package nl.hannahsten.texifyidea.inspections.latex.probablebugs

import nl.hannahsten.texifyidea.file.LatexFileType
import nl.hannahsten.texifyidea.inspections.TexifyInspectionTestBase

class LatexBibinputsRelativePathInspectionTest : TexifyInspectionTestBase(LatexBibinputsRelativePathInspection()) {

    fun testWarning() {
        myFixture.configureByText(LatexFileType, """
            \bibliography{../references}
        """.trimIndent())
        myFixture.checkHighlighting()
    }
}