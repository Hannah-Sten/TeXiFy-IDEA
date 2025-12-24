package nl.hannahsten.texifyidea.reference

import com.intellij.testFramework.fixtures.BasePlatformTestCase

class BibtexIdCompletionTest : BasePlatformTestCase() {

    override fun getTestDataPath(): String = "test/resources/completion/cite"

    @Throws(Exception::class)
    override fun setUp() {
        super.setUp()
    }

    // TODO(TEX-213) Fix tests using file set cache
    fun testCompleteLatexReferences() {
        // when
//        myFixture.configureByFilesAndBuildFilesets("${getTestName(false)}.tex", "bibtex.bib")
//            val result = myFixture.lookupElements!!

        // then
//            assertEquals(3, result.size)
//            val entry1 = result.first { l -> l!!.lookupString == "Evans2015" }!!
//            assertTrue(entry1.allLookupStrings.contains("Evans, Isaac"))
//            assertTrue(entry1.allLookupStrings.contains("Evans2015"))
//            assertTrue(entry1.allLookupStrings.contains("{Missing the Point(er): On the Effectiveness of Code Pointer Integrity}"))
    }
//    fun testCompletionResultsLowerCase() {
//        try {
//            // when
//            myFixture.configureByFilesWithMockCache("${getTestName(false)}.tex", "bibtex.bib")
//            val result = myFixture.lookupElementStrings
//
//            // then
//            assertEquals(1, result?.size)
//            assertTrue(result?.contains("Muchnick1997") == true)
//        }
//        finally {
//            clearAllMocks()
//            unmockkAll()
//        }
//    }
//
//    fun testCompletionResultsSecondEntry() {
//        try {
//            // when
//            myFixture.configureByFilesWithMockCache("${getTestName(false)}.tex", "bibtex.bib")
//            val result = myFixture.lookupElementStrings
//
//            // then
//            assertEquals(3, result?.size)
//            assertTrue(result?.contains("Muchnick1997") == true)
//            assertTrue(result?.contains("Evans2015") == true)
//            assertTrue(result?.contains("Burow2016") == true)
//        }
//        finally {
//            clearAllMocks()
//            unmockkAll()
//        }
//    }
//
//    fun testCompleteBibtexWithCorrectCase() {
//        try {
//            // Using the following failed sometimes
//            val testName = getTestName(false)
//            myFixture.configureByFilesWithMockCache("${testName}_before.tex", "$testName.bib")
//            myFixture.complete(CompletionType.BASIC)
//            myFixture.checkResultByFile("${testName}_after.tex")
//        }
//        finally {
//            clearAllMocks()
//            unmockkAll()
//        }
//    }
//
//    fun testBibtexEntryDocumentation() {
//        try {
//            myFixture.configureByFilesWithMockCache("${getTestName(false)}.tex", "bibtex.bib")
//            val element = DocumentationManager.getInstance(myFixture.project).getElementFromLookup(myFixture.editor, myFixture.file)
//
//            // Get the provider from the parent. Otherwise we request the documentation provider for a BibtexId element and, therefore,
//            // receive a BibtexDocumentationProvider instead of the LatexDocumentationProvider.
//            val provider = DocumentationManager.getProviderFromElement((myFixture.elementAtCaret.parent))
//
//            val documentation = provider.generateDoc(element, null)
//            assertNotNull(documentation)
//            assertTrue(documentation!!.contains("Code Pointer Integrity"))
//            assertTrue(documentation.contains("Evans"))
//            assertTrue(documentation.contains("have been known for decades"))
//        }
//        finally {
//            clearAllMocks()
//            unmockkAll()
//        }
//    }
}
