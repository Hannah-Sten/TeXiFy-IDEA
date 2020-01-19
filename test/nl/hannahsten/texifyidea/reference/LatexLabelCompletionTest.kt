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
        val testName = getTestName(false)
        myFixture.configureByFiles("$testName.tex", "bibtex.bib")

        // when
        val result = myFixture.complete(CompletionType.BASIC)

        // then
        assertEquals(3, result.size)
        val entry1 = result.first { l -> l.lookupString == "Evans2015" }
        assertTrue(entry1.allLookupStrings.contains("Evans, Isaac"))
        assertTrue(entry1.allLookupStrings.contains("Evans2015"))
        assertTrue(entry1.allLookupStrings.contains("Missing the Point(er): On the Effectiveness of Code Pointer Integrity"))
    }

    @Test
    fun testCompletionResultsLowerCase() {
        // given
        myFixture.configureByFiles("${getTestName(false)}.tex", "bibtex.bib")

        // when
        val result = myFixture.complete(CompletionType.BASIC)

        // then
        assertEquals(1, result.size)
        assertTrue(result.any { l -> l.lookupString == "Muchnick1997" })
    }

    @Test
    // see bug #1180
    fun testCompleteBibtexWithCorrectCase() {
        val testName = getTestName(false)
        myFixture.testCompletion("${testName}_before.tex", "${testName}_after.tex", "$testName.bib")
    }
}