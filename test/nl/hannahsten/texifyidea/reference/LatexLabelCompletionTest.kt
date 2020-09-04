package nl.hannahsten.texifyidea.reference

import com.intellij.codeInsight.completion.CompletionType
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import nl.hannahsten.texifyidea.file.LatexFileType
import org.junit.Test

class LatexLabelCompletionTest : BasePlatformTestCase() {
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

    private fun runCompletion() {
        myFixture.configureByFiles("${getTestName(false)}.tex", "bibtex.bib")

        // when
        myFixture.complete(CompletionType.BASIC)
    }

    @Test
    fun testCompleteBibtexWithCorrectCase() {
        // Using the following failed sometimes
        val testName = getTestName(false)
        myFixture.testCompletion("${testName}_before.tex", "${testName}_after.tex", "$testName.bib")
    }

    @Test
    fun testLabelReferenceCompletion() {
        // given
        myFixture.configureByText(LatexFileType, """
            \begin{document}
                \begin{figure}
                    \label{fig:figure}
                \end{figure}
                \begin{lstlisting}[label={lst:listing}]
                    Some text
                \end{lstlisting}
                \section{some section}
                \label{sec:some-section}
                \ref{<caret>}
            \end{document}
        """.trimIndent())

        // when
        val result = myFixture.complete(CompletionType.BASIC)

        // then
        assertEquals(3, result.size)
        assertTrue(result.any { l -> l.lookupString == "fig:figure" })
        assertTrue(result.any { l -> l.lookupString == "lst:listing" })
        assertTrue(result.any { l -> l.lookupString == "sec:some-section" })
    }
}