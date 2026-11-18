package nl.hannahsten.texifyidea.inspections.grazie

import com.intellij.testFramework.fixtures.BasePlatformTestCase
import nl.hannahsten.texifyidea.file.LatexFileType
import nl.hannahsten.texifyidea.psi.LatexContent
import nl.hannahsten.texifyidea.util.parser.findFirstChildOfType

class LatexCommaExtractionTest : BasePlatformTestCase() {

    private fun submittedText(source: String): String {
        myFixture.configureByText(LatexFileType, source)
        val root = myFixture.file.findFirstChildOfType(LatexContent::class)!!
        return LatexTextExtractor().buildTextContent(root).toString()
    }

    fun testPlainTextComma() {
        val sentence = "Thus, this is the case."
        assertEquals(sentence, submittedText(sentence))
    }

    fun testCommaInTextArgument() {
        val sentence = "Thus, this is the case."
        assertEquals(sentence, submittedText("\\textbf{$sentence}"))
    }

    fun testCommaInNestedTextArgument() {
        assertEquals("Thus, this is the case.", submittedText("\\textbf{{Thus, this is the case.}}"))
    }

    fun testProseBlockFollowingHeading() {
        // A brace-delimited prose block after a heading is parsed as another argument.
        val source = """
            \subsubsection{Low-Latency Adaptations}

            {
            Thus, this is the case.
            Thus, \ac{ABC} is the case~\cite{Test123}.
            }
        """.trimIndent()
        val result = submittedText(source)
        assertTrue(result, result.contains("Thus, this is the case."))
        assertTrue(result, result.contains("Thus, ABC is the case"))
        assertFalse(result, result.contains("Test123"))
    }

    fun testMissingCommaRemainsMissing() {
        assertEquals("Thus this is the case.", submittedText("\\textbf{Thus this is the case.}"))
    }

    fun testRepeatedCommasArePreserved() {
        assertEquals("Thus,, this is the case.", submittedText("\\textbf{Thus,, this is the case.}"))
    }
}