package nl.hannahsten.texifyidea.run.latex.steplog

import com.intellij.testFramework.fixtures.BasePlatformTestCase
import nl.hannahsten.texifyidea.run.latex.logtab.LatexLogMessage
import nl.hannahsten.texifyidea.run.latex.logtab.LatexLogMessageType

class LatexStepMessageParserSessionTest : BasePlatformTestCase() {

    fun testSupportsStructuredMessages() {
        val session = LatexStepMessageParserSession(mainFile = null)
        assertTrue(session.supportsStructuredMessages)
    }

    fun testEmptyInputProducesNoMessages() {
        val session = LatexStepMessageParserSession(mainFile = null)
        assertTrue(session.onText("").isEmpty())
    }

    fun testChunkedInputDoesNotThrow() {
        val session = LatexStepMessageParserSession(mainFile = null)

        val first = session.onText("LaTeX Warning: Reference `fig:bla' on page 1 undefined")
        val second = session.onText(" on input line 10.\n")

        assertTrue(first.isEmpty())
        assertNotNull(second)
    }

    fun testSecondLatexmkRunEmitsResetEvent() {
        val session = LatexStepMessageParserSession(mainFile = null)

        val firstRun = session.onText("Run number 1 of rule 'pdflatex'\n")
        val secondRun = session.onText("Run number 2 of rule 'pdflatex'\n")

        assertTrue(firstRun.isEmpty())
        assertEquals(listOf(ParsedStepEvent.ResetLatexMessages), secondRun)
    }

    fun testSecondLatexmkRunCanReemitSameWarningAfterReset() {
        val session = LatexStepMessageParserSession(mainFile = null)
        val warning = LatexLogMessage(
            message = "Reference `fig:bla' undefined",
            line = 10,
            type = LatexLogMessageType.WARNING,
        )

        val firstWarning = emitIfNew(session, warning)
        val reset = session.onText("Run number 2 of rule 'pdflatex'\n")
        val secondWarning = emitIfNew(session, warning)

        assertEquals(1, firstWarning.filterIsInstance<ParsedStepEvent.Message>().size)
        assertEquals(listOf(ParsedStepEvent.ResetLatexMessages), reset)
        assertEquals(firstWarning, secondWarning)
    }

    fun testLatexmkBibtexWarningIsParsedAsStructuredMessage() {
        val session = LatexStepMessageParserSession(mainFile = null)

        val outputs = buildList {
            addAll(session.onText("Latexmk: applying rule 'bibtex bibtex-mwe'...\n"))
            addAll(session.onText("For rule 'bibtex bibtex-mwe', running '&run_bibtex(  )' ...\n"))
            addAll(session.onText("This is BibTeX, Version 0.99d (TeX Live 2020)\n"))
            addAll(session.onText("The top-level auxiliary file: bibtex-mwe.aux\n"))
            addAll(session.onText("The style file: plain.bst\n"))
            addAll(session.onText("Database file #1: references.bib\n"))
            addAll(session.onText("Warning--I'm ignoring knuth1990's extra \"author\" field\n"))
            addAll(session.onText("--line 5 of file references.bib\n"))
            addAll(session.onText("(There was 1 warning)\n"))
        }

        val messages = outputs.filterIsInstance<ParsedStepEvent.Message>().map { it.message }
        assertEquals(
            listOf(
                ParsedStepMessage(
                    message = "I'm ignoring knuth1990's extra \"author\" field",
                    level = ParsedStepMessageLevel.WARNING,
                    fileName = "references.bib",
                    line = 5,
                    source = ParsedStepMessageSource.BIBTEX,
                )
            ),
            messages
        )
    }

    private fun emitIfNew(session: LatexStepMessageParserSession, message: LatexLogMessage): List<ParsedStepEvent> {
        val method = session.javaClass.getDeclaredMethod("emitIfNew", LatexLogMessage::class.java)
        method.isAccessible = true
        @Suppress("UNCHECKED_CAST")
        return method.invoke(session, message) as List<ParsedStepEvent>
    }
}
