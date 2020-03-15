package nl.hannahsten.texifyidea.run

import com.intellij.testFramework.fixtures.BasePlatformTestCase
import nl.hannahsten.texifyidea.run.latex.logtab.LatexLogMessage
import nl.hannahsten.texifyidea.run.latex.logtab.LatexLogMessageExtractor
import nl.hannahsten.texifyidea.run.latex.logtab.LatexLogMessageType.ERROR
import nl.hannahsten.texifyidea.run.latex.logtab.LatexLogMessageType.WARNING

class LatexMessageExtractorTest : BasePlatformTestCase() {
    val currentFile = "test.tex"

    /*
     * ERRORS
     */
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

    fun testUndefinedControlSequence() {
        val text = """./nested/lipsum-one.tex:9: Undefined control sequence.
        l.9 \bloop"""
        val expected = LatexLogMessage("Undefined control sequence. \\bloop", "./nested/lipsum-one.tex", 9, ERROR)
        testMessageExtractor(text, expected)
    }

    fun testPackageError() {
        val text = """./errors.tex:8: Package amsmath Error: \begin{split} won't work here."""
        val expected = LatexLogMessage("amsmath: \\begin{split} won't work here.", "./errors.tex", 8, ERROR)
        testMessageExtractor(text, expected)
    }

    /*
     * WARNINGS
     */
    fun testPackageWarning() {
        val text = "Package biblatex Warning: Please (re)run Biber on the file:(biblatex)     main"
        val expected = LatexLogMessage("biblatex: Please (re)run Biber on the file: main", currentFile, null, WARNING)
        testMessageExtractor(text, expected)
    }

    fun testUnresolvedReferencesWarning() {
        val text = "LaTeX Warning: There were undefined references."
        val expected = LatexLogMessage("There were undefined references.", currentFile, null, WARNING)
        testMessageExtractor(text, expected)
    }

    fun testIncompleteUndefinedControlSequence() {
        val text = "./nested/lipsum-one.tex:9: Undefined control sequence."
        val expected = null
        testMessageExtractor(text, expected, text)
    }

    fun testReferenceOnLine() {
        val text = "LaTeX Warning: Reference `fig:bla' on page 1 undefined on input line 10."
        val expected = LatexLogMessage("Reference `fig:bla' undefined", currentFile, 10, WARNING)
        testMessageExtractor(text, expected)
    }

    fun testEndInGroup() {
        val text = "(\\end occurred inside a group at level 1)"
        val expected = LatexLogMessage("(\\end occurred inside a group at level 1)", currentFile, null, WARNING)
        testMessageExtractor(text, expected)
    }

    fun testEndOccured() {
        val text = "(\\end occurred when \\iftrue on line 4 was incomplete)"
        val expected = LatexLogMessage("(\\end occurred when \\iftrue on line 4 was incomplete)", currentFile, null, WARNING)
        testMessageExtractor(text, expected)
    }

    fun testFileAlreadyExists() {
        val text = "LaTeX Warning: File `included.tex' already exists on the system.               Not generating it from this source."
        val expected = LatexLogMessage("File `included.tex' already exists on the system. Not generating it from this source.", currentFile, null, WARNING)
        testMessageExtractor(text, expected)
    }

    fun testFontSizeNotAvailable() {
        val text = "LaTeX Font Warning: Font shape `OT1/cmr/m/n' in size <42> not available(Font)              size <24.88> substituted on input line 5."
        val expected = LatexLogMessage("Font shape `OT1/cmr/m/n' in size <42> not available size <24.88> substituted", currentFile, 5, WARNING)
        testMessageExtractor(text, expected)
    }

    fun testFontShapeUndefined() {
        val text = "LaTeX Font Warning: Font shape `OT1/cmtt/b/n' undefined(Font)              using `OT1/cmtt/m/n' instead on input line 3."
        val expected = LatexLogMessage("Font shape `OT1/cmtt/b/n' undefined using `OT1/cmtt/m/n' instead", currentFile, 3, WARNING)
        testMessageExtractor(text, expected)
    }

    fun testFileContents() {
        val text = "LaTeX Warning: Writing text `    ' before \\end{filecontents}               as last line of filecontents.tex on input line 5."
        val expected = LatexLogMessage("Writing text `    ' before \\end{filecontents} as last line of filecontents.tex", currentFile, 5, WARNING)
        testMessageExtractor(text, expected)
    }

    fun testEmptyBracketRemoval() {
        val text = "Overfull \\hbox (252.50682pt too wide) in paragraph at lines 5--6[][]"
        val expected = LatexLogMessage("Overfull \\hbox (252.50682pt too wide) in paragraph at lines 5--6", currentFile, null, WARNING)
        testMessageExtractor(text, expected)
    }

    private fun testMessageExtractor(text: String, expected: LatexLogMessage?, newText: String = "") {
        val real = LatexLogMessageExtractor.findMessage(text, newText, currentFile)
        assertEquals(expected, real)
    }
}