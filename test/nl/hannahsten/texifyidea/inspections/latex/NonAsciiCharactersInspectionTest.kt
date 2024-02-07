package nl.hannahsten.texifyidea.inspections.latex

import com.intellij.codeInspection.NonAsciiCharactersInspection
import nl.hannahsten.texifyidea.file.LatexFileType
import nl.hannahsten.texifyidea.inspections.TexifyInspectionTestBase

class NonAsciiCharactersInspectionTest : TexifyInspectionTestBase(NonAsciiCharactersInspection()) {

    fun testWarning() {
        myFixture.configureByText(
            LatexFileType,
            """
            \textbf{erhöhen}
            \label{sec:<warning descr="Non-ASCII characters"><warning descr="Non-ASCII symbols in ASCII word">Название</warning></warning>}
            """.trimIndent()
        )
        myFixture.checkHighlighting()
    }
}