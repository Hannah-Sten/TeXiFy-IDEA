package nl.hannahsten.texifyidea.run.latex.steplog

import com.intellij.testFramework.fixtures.BasePlatformTestCase

class BibtexStepMessageParserSessionTest : BasePlatformTestCase() {

    fun testParsesBibtexWarning() {
        val session = BibtexStepMessageParserSession(mainFile = null)

        val messages = mutableListOf<ParsedStepMessage>()
        messages += session.onText("Database file #1: references.bib\n")
        messages += session.onText("Warning--I didn't find a database entry for \"knuth19902\"\n")
        messages += session.onText("(There was 1 warning)\n")

        assertEquals(1, messages.size)
        val message = messages.single()
        assertEquals(ParsedStepMessageLevel.WARNING, message.level)
        assertEquals("references.bib", message.fileName)
        assertTrue(message.message.contains("I didn't find a database entry"))
    }

    fun testParsesBibtexErrorWithLineNumber() {
        val session = BibtexStepMessageParserSession(mainFile = null)

        val messages = mutableListOf<ParsedStepMessage>()
        messages += session.onText("This is BibTeX, Version 0.99d (TeX Live 2020)\n")
        messages += session.onText("The top-level auxiliary file: bibtex-mwe.aux\n")
        messages += session.onText("I couldn't open database file references34.bib\n")
        messages += session.onText("---line 3 of file bibtex-mwe.aux\n")
        messages += session.onText(" : \\bibdata{references34\n")
        messages += session.onText(" :                      }\n")
        messages += session.onText("I'm skipping whatever remains of this command\n")

        assertEquals(1, messages.size)
        val message = messages.single()
        assertEquals(ParsedStepMessageLevel.ERROR, message.level)
        assertEquals("bibtex-mwe.aux", message.fileName)
        assertEquals(3, message.line)
        assertTrue(message.message.contains("I couldn't open database file"))
    }

    fun testBuffersIncompleteLineUntilNewline() {
        val session = BibtexStepMessageParserSession(mainFile = null)

        val first = session.onText("Database file #1: references.bib")
        val second = session.onText("\nWarning--I didn't find a database entry for \"knuth19902\"\n")

        assertTrue(first.isEmpty())
        assertEquals(1, second.size)
        assertEquals(ParsedStepMessageLevel.WARNING, second.single().level)
    }
}
