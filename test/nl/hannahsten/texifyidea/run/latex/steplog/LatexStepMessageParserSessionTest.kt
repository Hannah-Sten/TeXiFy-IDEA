package nl.hannahsten.texifyidea.run.latex.steplog

import com.intellij.testFramework.fixtures.BasePlatformTestCase

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
}
