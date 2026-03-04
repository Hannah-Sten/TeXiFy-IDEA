package nl.hannahsten.texifyidea.run.latex.steplog

import com.intellij.testFramework.fixtures.BasePlatformTestCase

class BibtexStepMessageParserSessionTest : BasePlatformTestCase() {

    fun testSupportsStructuredMessages() {
        val session = BibtexStepMessageParserSession(mainFile = null)
        assertTrue(session.supportsStructuredMessages)
    }

    fun testEmptyInputProducesNoMessages() {
        val session = BibtexStepMessageParserSession(mainFile = null)
        assertTrue(session.onText("").isEmpty())
    }

    fun testChunkedInputDoesNotThrow() {
        val session = BibtexStepMessageParserSession(mainFile = null)

        val first = session.onText("Database file #1: references.bib")
        val second = session.onText("\nWarning--I didn't find a database entry for \"knuth19902\"\n")

        assertTrue(first.isEmpty())
        assertNotNull(second)
    }
}
