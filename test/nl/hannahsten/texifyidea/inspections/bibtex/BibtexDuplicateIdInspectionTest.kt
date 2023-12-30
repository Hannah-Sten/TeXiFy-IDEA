package nl.hannahsten.texifyidea.inspections.bibtex

import nl.hannahsten.texifyidea.file.BibtexFileType
import nl.hannahsten.texifyidea.inspections.TexifyInspectionTestBase

class BibtexDuplicateIdInspectionTest : TexifyInspectionTestBase(BibtexDuplicateIdInspection()) {

    fun `test @strings are not always duplicate ids`() {
        myFixture.configureByText(
            BibtexFileType,
            """
            @string{ a = test }

            @string{ b = c }
            """.trimIndent()
        )
        myFixture.checkHighlighting()
    }

    fun `test duplicate @strings`() {
        myFixture.configureByText(
            BibtexFileType,
            """
            <error descr="Duplicate identifier 'a'">@string{ a = test </error>}

            <error descr="Duplicate identifier 'a'">@string{ a = c </error>}
            """.trimIndent()
        )
        myFixture.checkHighlighting()
    }
}