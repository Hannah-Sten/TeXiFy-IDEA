package nl.hannahsten.texifyidea.reference

import com.intellij.codeInsight.completion.CompletionType
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import org.junit.Test

class LatexLabelCompletionTest : BasePlatformTestCase() {
    override fun getTestDataPath(): String {
        return "testData/completion/cite"
    }

    @Throws(Exception::class)
    override fun setUp() {
        super.setUp()
    }

    @Test
    fun testCompleteLatexReferences(){
        // given
        myFixture.configureByFiles("main.tex", "bibtex.bib")

        // when
        val result = myFixture.complete(CompletionType.BASIC)

        // then
        assertEquals(3, result.size)
        val entry1 = result.first { l -> l.lookupString == "Evans2015" }
        assertTrue(entry1.allLookupStrings.contains("Evans, Isaac"))
        assertTrue(entry1.allLookupStrings.contains("Evans2015"))
        assertTrue(entry1.allLookupStrings.contains("Missing the Point(er): On the Effectiveness of Code Pointer Integrity"))
        assertFalse(entry1.isCaseSensitive)
    }
}