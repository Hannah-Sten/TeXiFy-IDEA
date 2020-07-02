package nl.hannahsten.texifyidea.inspections.latex

import com.intellij.codeInspection.NonAsciiCharactersInspection
import nl.hannahsten.texifyidea.file.LatexFileType
import nl.hannahsten.texifyidea.inspections.TexifyInspectionTestBase

class NonAsciiCharactersInspectionTest : TexifyInspectionTestBase(NonAsciiCharactersInspection()) {
    fun testWarning() {
        myFixture.configureByText(LatexFileType, """
            \textbf{erhöhen}
            \label{<warning descr="Identifier contains symbols from different languages: [LATIN, CYRILLIC]"><warning descr="Non-ASCII characters in an identifier">sec:Название</warning></warning>}
        """.trimIndent())
        myFixture.checkHighlighting()
    }
}