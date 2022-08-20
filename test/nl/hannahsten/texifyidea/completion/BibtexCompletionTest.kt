package nl.hannahsten.texifyidea.completion

import com.intellij.codeInsight.completion.CompletionType
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import nl.hannahsten.texifyidea.file.BibtexFileType

class BibtexCompletionTest : BasePlatformTestCase() {

    fun testArticle() {
        myFixture.configureByText(BibtexFileType, """@art<caret>""")
        val result = myFixture.complete(CompletionType.BASIC)
        // Assumes that article will appear twice in the autocomplete
        assertTrue("Bibtex autocompletion should be available", result.any { it.lookupString.contains("article") })
    }

    fun testPreamble() {
        myFixture.configureByText(BibtexFileType, """@prea<caret>""")
        myFixture.complete(CompletionType.BASIC)
        myFixture.checkResult(
            """
            @preamble{
                ""
            }
            """.trimIndent()
        )
    }
}