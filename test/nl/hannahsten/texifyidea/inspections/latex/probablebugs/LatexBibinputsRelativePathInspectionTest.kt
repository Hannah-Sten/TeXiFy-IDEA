package nl.hannahsten.texifyidea.inspections.latex.probablebugs

import nl.hannahsten.texifyidea.file.LatexFileType
import nl.hannahsten.texifyidea.inspections.TexifyInspectionTestBase

class LatexBibinputsRelativePathInspectionTest : TexifyInspectionTestBase(LatexBibinputsRelativePathInspection()) {

    fun testWarning() {
        // Unfortunately I don't yet know how to trigger this inspection in tests because it depends on run configurations
        myFixture.configureByText(LatexFileType, """
            \bibliography{../references}
        """.trimIndent())
        myFixture.checkHighlighting()
    }
}