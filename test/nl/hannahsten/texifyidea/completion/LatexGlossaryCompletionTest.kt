package nl.hannahsten.texifyidea.completion

import com.intellij.codeInsight.completion.CompletionType
import com.intellij.codeInsight.lookup.LookupElement
import com.intellij.codeInsight.lookup.LookupElementPresentation
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import nl.hannahsten.texifyidea.file.LatexFileType
import nl.hannahsten.texifyidea.updateCommandDef

class LatexGlossaryCompletionTest : BasePlatformTestCase() {

    override fun getTestDataPath(): String {
        return "test/resources/completion/glossary"
    }

    private fun validateLookupElement(
        lookup: String,
        short: String,
        description: String,
        elements: Array<LookupElement>
    ) {
        val element = elements.singleOrNull { l -> l.lookupString == lookup }
        assertNotNull("$lookup glossary entry not found", element)
        val presentation = LookupElementPresentation()
        element!!.renderElement(presentation)
        assertTrue("$lookup short description not found", presentation.typeText?.contains(short) ?: false)
        assertTrue("$lookup long description not found", presentation.tailText?.contains(description) ?: false)
    }

    fun testCompleteGlossaryCommandEntries() {
        // given
        myFixture.configureByFiles("${getTestName(false)}.tex")
        myFixture.updateCommandDef()
        // when
        val result = myFixture.complete(CompletionType.BASIC)

        // then
        assertEquals(4, result.size)
        validateLookupElement("aslr", "ASLR", "Address Space Layout Randomization", result)
        validateLookupElement("maths", "mathematics", "what mathematicians do", result)
        validateLookupElement("latex", "latex", "scientific documents", result)
        validateLookupElement("fishage", "Fish Age", "spanning from the end", result)
    }

    private fun testGlossaryReferenceCompletion(command: String) {
        // given
        myFixture.configureByText(
            LatexFileType,
            """
            \usepackage{glossaries}
            \newacronym{aslr}{ASLR}{Address Space Layout Randomization}
            \begin{document}
                \$command{<caret>}
            \end{document}
            """.trimIndent()
        )
        myFixture.updateCommandDef()

        // when
        val result = myFixture.complete(CompletionType.BASIC)

        // then
        assertEquals(1, result.size)
        assertTrue(result.any { l -> l.lookupString == "aslr" })
    }

    fun testCompleteGlossaryReferences() {
        testGlossaryReferenceCompletion("gls")
        testGlossaryReferenceCompletion("Gls")
        testGlossaryReferenceCompletion("glspl")
        testGlossaryReferenceCompletion("Gls")
    }

    // TODO(TEX-213) Fix tests using file set cache
//    fun testExternalGlossaryCompletion() {
//        try {
//            // given
//            myFixture.configureByFilesWithMockCache("LoadExternalGlossary.tex", "glossar.tex")
//
//            // when
//            val result = myFixture.complete(CompletionType.BASIC)
//
//            // then
//            assertEquals(2, result.size)
//            assertTrue(result.any { l -> l.lookupString == "aslr" })
//            assertTrue(result.any { l -> l.lookupString == "maths" })
//        }
//        finally {
//            clearAllMocks()
//            unmockkAll()
//        }
//    }
}