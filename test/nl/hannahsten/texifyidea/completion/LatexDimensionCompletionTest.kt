package nl.hannahsten.texifyidea.completion

import com.intellij.codeInsight.completion.CompletionType
import com.intellij.codeInsight.lookup.Lookup
import com.intellij.codeInsight.lookup.LookupElementPresentation
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import nl.hannahsten.texifyidea.file.LatexFileType

class LatexDimensionCompletionTest : BasePlatformTestCase() {

    fun `test dimension completion after numeric prefix`() {
        myFixture.configureByText(LatexFileType, """\vspace{10<caret>}""")

        val result = myFixture.complete(CompletionType.BASIC)

        assertNotNull(result)
        assertTrue(result.any { it.lookupString == "10pt" })
        assertTrue(result.any { it.lookupString == "10em" })

        val ptItem = result.single { it.lookupString == "10pt" }
        val presentation = LookupElementPresentation()
        ptItem.renderElement(presentation)
        assertEquals("pt", presentation.itemText)
        assertTrue(presentation.typeText?.contains("point") == true)
    }

    fun `test dimension completion filters by typed unit prefix`() {
        myFixture.configureByText(LatexFileType, """\vspace{10p<caret>}""")

        val result = myFixture.complete(CompletionType.BASIC)

        assertNotNull(result)
        assertTrue(result.any { it.lookupString == "10pt" })
        assertTrue(result.any { it.lookupString == "10pc" })
        assertFalse(result.any { it.lookupString == "10em" })
    }

    fun `test non dimension context does not suggest dimension units`() {
        myFixture.configureByText(LatexFileType, """\textbf{10<caret>}""")

        val result = myFixture.complete(CompletionType.BASIC) ?: emptyArray()
        assertFalse(result.any { it.lookupString == "10pt" })
        assertFalse(result.any { it.lookupString == "10em" })
    }

    fun `test dimension completion insertion`() {
        myFixture.configureByText(LatexFileType, """\vspace{10b<caret>}""")

        val result = myFixture.complete(CompletionType.BASIC)

        if (result == null) {
            myFixture.checkResult("""\vspace{10bp<caret>}""")
        }
        else {
            myFixture.finishLookup(Lookup.NORMAL_SELECT_CHAR)
            myFixture.checkResult("""\vspace{10bp}""")
        }
    }
}
