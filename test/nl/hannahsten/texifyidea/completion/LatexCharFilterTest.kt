package nl.hannahsten.texifyidea.completion

import com.intellij.codeInsight.completion.CompletionType
import com.intellij.codeInsight.lookup.CharFilter
import com.intellij.codeInsight.lookup.LookupManager
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import nl.hannahsten.texifyidea.file.LatexFileType
import nl.hannahsten.texifyidea.updateCommandDef

class LatexCharFilterTest : BasePlatformTestCase() {

    private val charFilter = LatexCharFilter()

    override fun getTestDataPath(): String = "test/resources/completion/cite"

    fun testSpaceAddsToPrefix_inCiteContext() {
        // Set up a cite command context with actual bibtex entries
        myFixture.configureByFiles("CompleteLatexReferences.tex", "bibtex.bib")
        myFixture.updateCommandDef()
        myFixture.complete(CompletionType.BASIC)
        val lookup = LookupManager.getActiveLookup(myFixture.editor)
        assertNotNull("Lookup should be active", lookup)

        // Space should add to prefix in cite context
        val result = charFilter.acceptChar(' ', 0, lookup!!)
        assertEquals(
            "Space should add to prefix in cite context",
            CharFilter.Result.ADD_TO_PREFIX,
            result
        )
    }

    fun testSpaceDefaultBehavior_outsideCiteContext() {
        // Set up a non-cite command context (regular command completion)
        myFixture.configureByText(
            LatexFileType,
            """
            \documentclass{article}
            \begin{document}
            \sec<caret>
            \end{document}
            """.trimIndent()
        )
        myFixture.complete(CompletionType.BASIC)
        val lookup = LookupManager.getActiveLookup(myFixture.editor)
        assertNotNull("Lookup should be active", lookup)

        // Space should use default behavior (null) outside cite context
        val result = charFilter.acceptChar(' ', 0, lookup!!)
        assertNull(
            "Space should use default behavior outside cite context",
            result
        )
    }

    fun testColonAddsToPrefix_inCiteContext() {
        // Use cite context which has completion results
        myFixture.configureByFiles("CompleteLatexReferences.tex", "bibtex.bib")
        myFixture.updateCommandDef()
        myFixture.complete(CompletionType.BASIC)
        val lookup = LookupManager.getActiveLookup(myFixture.editor)
        assertNotNull("Lookup should be active", lookup)

        // Colon should always add to prefix
        val result = charFilter.acceptChar(':', 0, lookup!!)
        assertEquals(
            "Colon should add to prefix",
            CharFilter.Result.ADD_TO_PREFIX,
            result
        )
    }

    fun testSpaceAddsToPrefix_inUserDefinedCiteAlias() {
        myFixture.configureByText(
            LatexFileType,
            """
            \begin{thebibliography}{9}
                \bibitem{testkey}
                Reference.
            \end{thebibliography}

            \newcommand{\mycite}[1]{\cite{#1}}

            \mycite{<caret>}
            """.trimIndent()
        )
        myFixture.updateCommandDef()
        myFixture.complete(CompletionType.BASIC)
        val lookup = LookupManager.getActiveLookup(myFixture.editor)
        assertNotNull("Lookup should be active", lookup)

        val result = charFilter.acceptChar(' ', 0, lookup!!)
        assertEquals(
            "Space should add to prefix in user-defined cite alias context",
            CharFilter.Result.ADD_TO_PREFIX,
            result
        )
    }

    fun testDollarHidesLookup() {
        myFixture.configureByText(
            LatexFileType,
            """
            \documentclass{article}
            \begin{document}
            \sec<caret>
            \end{document}
            """.trimIndent()
        )
        myFixture.complete(CompletionType.BASIC)
        val lookup = LookupManager.getActiveLookup(myFixture.editor)
        assertNotNull("Lookup should be active", lookup)

        // Dollar should hide the lookup (entering math mode)
        val result = charFilter.acceptChar('$', 0, lookup!!)
        assertEquals(
            "Dollar should hide lookup",
            CharFilter.Result.HIDE_LOOKUP,
            result
        )
    }
}
