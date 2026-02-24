package nl.hannahsten.texifyidea.run.latex.steplog

import com.intellij.testFramework.fixtures.BasePlatformTestCase

class LatexStepMessageParserSessionTest : BasePlatformTestCase() {

    fun testParsesLatexErrorAndWarning() {
        val session = LatexStepMessageParserSession(mainFile = null)

        val messages = mutableListOf<ParsedStepMessage>()
        messages += session.onText("./main.tex:5: LaTeX Error: Encoding scheme `15' unknown.\n")
        messages += session.onText("LaTeX Warning: Citation 'DBLP.books.daglib.0076726' on page 1 undefined on input line 7.\n")

        assertTrue(messages.any { it.level == ParsedStepMessageLevel.ERROR && it.line == 5 && it.message.contains("Encoding scheme") })
        assertTrue(messages.any { it.level == ParsedStepMessageLevel.WARNING && it.line == 7 && it.message.contains("Citation") })
    }

    fun testParsesMultilineWarning() {
        val session = LatexStepMessageParserSession(mainFile = null)

        val messages = mutableListOf<ParsedStepMessage>()
        messages += session.onText("LaTeX Warning: You have requested, on input line 5, version\n")
        messages += session.onText("               `9999/99/99' of package test998,\n")
        messages += session.onText("               but only version\n")
        messages += session.onText("               `2020/04/08'\n")
        messages += session.onText("               is available.\n")
        messages += session.onText("\n")

        assertEquals(1, messages.size)
        val message = messages.single()
        assertEquals(ParsedStepMessageLevel.WARNING, message.level)
        assertEquals(5, message.line)
        assertTrue(message.message.contains("is available"))
    }

    fun testBuffersIncompleteLineUntilNewline() {
        val session = LatexStepMessageParserSession(mainFile = null)

        val first = session.onText("LaTeX Warning: Reference `fig:bla` on page 1 undefined on input")
        val second = session.onText(" line 10.\n")

        assertTrue(first.isEmpty())
        assertEquals(1, second.size)
        assertEquals(ParsedStepMessageLevel.WARNING, second.single().level)
        assertEquals(10, second.single().line)
    }
}
