package nl.hannahsten.texifyidea.reference

import com.intellij.codeInsight.completion.CompletionType
import com.intellij.codeInsight.documentation.DocumentationManager
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import io.mockk.*
import nl.hannahsten.texifyidea.remotelibraries.RemoteLibraryManager
import nl.hannahsten.texifyidea.remotelibraries.state.BibtexEntryListConverter
import nl.hannahsten.texifyidea.remotelibraries.state.LibraryState
import nl.hannahsten.texifyidea.remotelibraries.zotero.ZoteroLibrary
import org.junit.Test

class BibtexIdCompletionTest : BasePlatformTestCase() {

    override fun getTestDataPath(): String {
        return "test/resources/completion/cite"
    }

    @Throws(Exception::class)
    override fun setUp() {
        super.setUp()
    }

    @Test
    fun testCompleteLatexReferences() {
        // when
        runCompletion()
        val result = myFixture.lookupElements!!

        // then
        assertEquals(3, result.size)
        val entry1 = result.first { l -> l!!.lookupString == "Evans2015" }!!
        assertTrue(entry1.allLookupStrings.contains("Evans, Isaac"))
        assertTrue(entry1.allLookupStrings.contains("Evans2015"))
        assertTrue(entry1.allLookupStrings.contains("Missing the Point(er): On the Effectiveness of Code Pointer Integrity"))
    }

    @Test
    fun testCompletionResultsLowerCase() {
        // when
        runCompletion()
        val result = myFixture.lookupElementStrings

        // then
        assertEquals(1, result?.size)
        assertTrue(result?.contains("Muchnick1997") == true)
    }

    @Test
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

    @Test
    fun testCompleteBibtexWithCorrectCase() {
        // Using the following failed sometimes
        val testName = getTestName(false)
        myFixture.testCompletion("${testName}_before.tex", "${testName}_after.tex", "$testName.bib")
    }

    @Test
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

    @Test
    fun testCiteFromLibraryCompletion() {

        val remotebib = """
            @book{newey_how_2017,
                address = {London},
                title = {How to build a car},
                isbn = {9780008196806},
                language = {eng},
                publisher = {HarperCollins Publishers},
                author = {Newey, Adrian},
                year = {2017},
            }
        """.trimIndent()

        mockkObject(RemoteLibraryManager)
        every { RemoteLibraryManager.getInstance().getLibraries() } returns mutableMapOf("aaa" to LibraryState("mocked", ZoteroLibrary::class.java, BibtexEntryListConverter().fromString(remotebib)))

        myFixture.configureByFiles("library/${getTestName(false)}_before.tex", "library/bibtex_before.bib")

        myFixture.complete(CompletionType.BASIC)

        myFixture.checkResultByFile("library/${getTestName(false)}_before.tex", "library/${getTestName(false)}_after.tex", true)
        myFixture.checkResultByFile("library/bibtex_before.bib","library/bibtex_after.bib", true)
    }

    private fun runCompletion() {
        myFixture.configureByFiles("${getTestName(false)}.tex", "bibtex.bib")
        // when
        myFixture.complete(CompletionType.BASIC)
    }
}
