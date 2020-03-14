package nl.hannahsten.texifyidea.run

import com.intellij.testFramework.fixtures.BasePlatformTestCase
import nl.hannahsten.texifyidea.run.latex.logtab.LatexLogMessage
import nl.hannahsten.texifyidea.run.latex.logtab.LatexLogMessageExtractor
import nl.hannahsten.texifyidea.run.latex.logtab.LatexLogMessageType.ERROR
import nl.hannahsten.texifyidea.run.latex.logtab.LatexLogMessageType.WARNING

class LatexMessageExtractorTest : BasePlatformTestCase() {
    val currentFile = "test.tex"

    fun testEnvironmentUndefinedError() {
        val text = "./main.tex:1: LaTeX Error: Environment align undefined."
        val expected = LatexLogMessage("Environment align undefined.", "./main.tex", 1, ERROR)
        testMessageExtractor(text, expected)
    }

    fun testPackageNotInstalledError() {
        val text = "! LaTeX Error: File `paralisy.sty' not found."
        val expected = LatexLogMessage("File `paralisy.sty' not found.", currentFile, null, ERROR)
        testMessageExtractor(text, expected)
    }

    fun testPackageWarning() {
        val text = "Package biblatex Warning: Please (re)run Biber on the file:"
        val expected = LatexLogMessage("Package biblatex Warning: Please (re)run Biber on the file:", currentFile, null, WARNING)
        testMessageExtractor(text, expected)
    }

    fun testUnresolvedReferencesWarning() {
        val text = "LaTeX Warning: There were undefined references."
        val expected = LatexLogMessage("There were undefined references.", currentFile, null, WARNING)
        testMessageExtractor(text, expected)
    }

    fun testUndefinedControlSequence() {
        val text = """./nested/lipsum-one.tex:9: Undefined control sequence.
        l.9 \bloop"""
        val expected = LatexLogMessage("Undefined control sequence. \\bloop", "./nested/lipsum-one.tex", 9, ERROR)
        testMessageExtractor(text, expected)
    }

    fun testIncompleteUndefinedControlSequence() {
        val text = "./nested/lipsum-one.tex:9: Undefined control sequence."
        val expected = null
        testMessageExtractor(text, expected, text)
    }

    private fun testMessageExtractor(text: String, expected: LatexLogMessage?, newText: String = "") {
        val real = LatexLogMessageExtractor.findMessage(text, newText, currentFile)
        assertEquals(expected, real)
    }
}