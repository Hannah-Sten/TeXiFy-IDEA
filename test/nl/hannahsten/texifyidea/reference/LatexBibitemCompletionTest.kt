package nl.hannahsten.texifyidea.reference

import com.intellij.codeInsight.completion.CompletionType
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import org.junit.Test

class LatexBibitemCompletionTest : BasePlatformTestCase() {
    override fun getTestDataPath(): String {
        return "testData/completion/cite"
    }

    @Throws(Exception::class)
    override fun setUp() {
        super.setUp()
    }

    @Test
    fun testCompleteBibitemReferences(){
        // given
        val testName = getTestName(false)
        myFixture.configureByFiles("$testName.tex")

        // when
        val result = myFixture.complete(CompletionType.BASIC)

        // then
        assertEquals(1, result.size)
        val entry1 = result.first { l -> l.lookupString == "knuth1990" }
        assertTrue(entry1.allLookupStrings.contains("knuth1990"))
        assertTrue(entry1.allLookupStrings.contains("$testName.tex: 11"))
    }
}