package nl.hannahsten.texifyidea.reference

import com.intellij.codeInsight.completion.CompletionType
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import nl.hannahsten.texifyidea.documentation.LatexDocumentationProvider
import nl.hannahsten.texifyidea.updateCommandDef
import nl.hannahsten.texifyidea.updateFilesets

class BibtexIdCompletionTest : BasePlatformTestCase() {

    override fun getTestDataPath(): String = "test/resources/completion/cite"

    @Throws(Exception::class)
    override fun setUp() {
        super.setUp()
    }

    fun testCompleteLatexReferences() {
        // when
        myFixture.configureByFiles("${getTestName(false)}.tex", "bibtex.bib")
        myFixture.updateCommandDef()
        val result = myFixture.completeBasic()

        // then
        assertEquals(3, result.size)
        val entry1 = result.first { l -> l!!.lookupString == "Evans2015" }!!
        assertTrue(entry1.allLookupStrings.contains("Evans, Isaac"))
        assertTrue(entry1.allLookupStrings.contains("Evans2015"))
        assertTrue(entry1.allLookupStrings.contains("{Missing the Point(er): On the Effectiveness of Code Pointer Integrity}"))
    }

    fun testCompletionResultsLowerCase() {
        // when
        myFixture.configureByFiles("${getTestName(false)}.tex", "bibtex.bib")
        myFixture.updateFilesets()
        val result = myFixture.completeBasic().mapNotNull { it.lookupString }

        // then
        assertEquals(1, result.size)
        assertTrue(result.contains("Muchnick1997"))
    }

    fun testCompletionResultsSecondEntry() {
        // when
        myFixture.configureByFiles("${getTestName(false)}.tex", "bibtex.bib")
        myFixture.updateFilesets()
        val result = myFixture.completeBasic().mapNotNull { it.lookupString }

        // then
        assertEquals(3, result.size)
        assertTrue(result.contains("Muchnick1997"))
        assertTrue(result.contains("Evans2015"))
        assertTrue(result.contains("Burow2016"))
    }

    fun testCompleteBibtexWithCorrectCase() {
        // Using the following failed sometimes
        val testName = getTestName(false)
        myFixture.configureByFiles("${testName}_before.tex", "$testName.bib")
        myFixture.updateCommandDef()
        myFixture.type("goossens")
        myFixture.complete(CompletionType.BASIC)
        myFixture.checkResultByFile("${testName}_after.tex")
    }

    fun testCompletionFiltersByMultipleTerms() {
        // Test that completion filters by multiple space-separated terms
        myFixture.configureByFiles("CompleteLatexReferences.tex", "bibtex.bib")
        myFixture.updateCommandDef()
        myFixture.completeBasic()
        myFixture.type("Evans Isaac")

        val result = myFixture.lookupElements?.mapNotNull { it.lookupString } ?: emptyList()
        assertEquals(1, result.size)
        assertTrue(result.contains("Evans2015"))
    }

    fun testBibtexReferenceDocumentation() {
        myFixture.configureByFiles("BibtexEntryDocumentation.tex", "bibtex.bib")
        myFixture.updateCommandDef()

        // Get the element at caret and resolve to the bibtex entry
        val reference = myFixture.getReferenceAtCaretPosition()
        assertNotNull("Expected a reference at caret position", reference)
        val resolved = reference!!.resolve()
        assertNotNull("Expected reference to resolve", resolved)

        // Generate documentation directly using the provider
        val provider = LatexDocumentationProvider()
        val documentation = provider.generateDoc(resolved!!, myFixture.elementAtCaret)

        assertNotNull(documentation)
        assertTrue(documentation!!.contains("Code Pointer Integrity"))
        assertTrue(documentation.contains("Evans"))
        assertTrue(documentation.contains("have been known for decades"))
    }
}