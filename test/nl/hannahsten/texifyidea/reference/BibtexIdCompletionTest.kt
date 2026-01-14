package nl.hannahsten.texifyidea.reference

import com.intellij.codeInsight.completion.CompletionType
import com.intellij.testFramework.fixtures.BasePlatformTestCase
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

    // DocumentationManagaer was removed from the api, unclear how to test the documentation popup now
//    fun testBibtexEntryDocumentation() {
//        myFixture.configureByFiles("${getTestName(false)}.tex", "bibtex.bib")
//        myFixture.updateFilesets()
//        val element = DocumentationManager.getInstance(myFixture.project).getElementFromLookup(myFixture.editor, myFixture.file)
//
//        // Get the provider from the parent. Otherwise we request the documentation provider for a BibtexId element and, therefore,
//        // receive a BibtexDocumentationProvider instead of the LatexDocumentationProvider.
//        val provider = DocumentationManager.getProviderFromElement((myFixture.elementAtCaret.parent))
//
//        val documentation = provider.generateDoc(element, null)
//        assertNotNull(documentation)
//        assertTrue(documentation!!.contains("Code Pointer Integrity"))
//        assertTrue(documentation.contains("Evans"))
//        assertTrue(documentation.contains("have been known for decades"))
//    }
}