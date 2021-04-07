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
            \label{<warning descr="Non-ASCII characters in an identifier"><warning descr="Symbols from different languages found: [LATIN, CYRILLIC]">sec:Название</warning></warning>}
            """.trimIndent()
        )
        myFixture.checkHighlighting()
    }
}