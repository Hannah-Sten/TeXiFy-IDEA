package nl.hannahsten.texifyidea.reference

import com.intellij.codeInsight.completion.CompletionType
import com.intellij.codeInsight.documentation.DocumentationManager
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import nl.hannahsten.texifyidea.util.files.ReferencedFileSetService

class BibtexIdCompletionTest : BasePlatformTestCase() {

    override fun getTestDataPath(): String {
        return "test/resources/completion/cite"
    }

    @Throws(Exception::class)
    override fun setUp() {
        super.setUp()
    }

    fun testCompleteLatexReferences() {
        // when
        runCompletion()
        val result = myFixture.lookupElements!!

        // then
        assertEquals(3, result.size)
        val entry1 = result.first { l -> l!!.lookupString == "Evans2015" }!!
        assertTrue(entry1.allLookupStrings.contains("Evans, Isaac"))
        assertTrue(entry1.allLookupStrings.contains("Evans2015"))
        assertTrue(entry1.allLookupStrings.contains("{Missing the Point(er): On the Effectiveness of Code Pointer Integrity}"))
    }

    fun testCompletionResultsLowerCase() {
        // when
        runCompletion()
        val result = myFixture.lookupElementStrings

        // then
        assertEquals(1, result?.size)
        assertTrue(result?.contains("Muchnick1997") == true)
    }

    fun testCompletionResultsSecondEntry() {
        // when
        runCompletion()
        val result = myFixture.lookupElementStrings

        // then
        assertEquals(3, result?.size)
        assertTrue(result?.contains("Muchnick1997") == true)
        assertTrue(result?.contains("Evans2015") == true)
        assertTrue(result?.contains("Burow2016") == true)
    }

    fun testCompleteBibtexWithCorrectCase() {
        // Using the following failed sometimes
        val testName = getTestName(false)
        myFixture.testCompletion("${testName}_before.tex", "${testName}_after.tex", "$testName.bib")
    }

    fun testBibtexEntryDocumentation() {
        runCompletion()
        val element = DocumentationManager.getInstance(myFixture.project).getElementFromLookup(myFixture.editor, myFixture.file)

        // Get the provider from the parent. Otherwise we request the documentation provider for a BibtexId element and, therefore,
        // receive a BibtexDocumentationProvider instead of the LatexDocumentationProvider.
        val provider = DocumentationManager.getProviderFromElement((myFixture.elementAtCaret.parent))

        val documentation = provider.generateDoc(element, null)
        assertNotNull(documentation)
        assertTrue(documentation!!.contains("Code Pointer Integrity"))
        assertTrue(documentation.contains("Evans"))
        assertTrue(documentation.contains("have been known for decades"))
    }

    private fun runCompletion() {
        val files = myFixture.configureByFiles("${getTestName(false)}.tex", "bibtex.bib")
        // The first time completion runs, due to caching there may be a race condition
        for (file in files) {
            ReferencedFileSetService.getInstance().referencedFileSetOf(file)
        }
        // when
        myFixture.complete(CompletionType.BASIC)
    }
}
