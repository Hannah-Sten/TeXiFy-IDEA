package nl.hannahsten.texifyidea.run.logtab

import com.intellij.testFramework.fixtures.BasePlatformTestCase
import nl.hannahsten.texifyidea.run.ui.console.logtab.LatexLogMessage
import nl.hannahsten.texifyidea.run.ui.console.logtab.LatexLogMessageExtractor
import nl.hannahsten.texifyidea.run.ui.console.logtab.LatexLogMessageType.ERROR
import nl.hannahsten.texifyidea.run.ui.console.logtab.LatexLogMessageType.WARNING
import nl.hannahsten.texifyidea.util.removeAll

/**
 * Tests for errors and warnings consisting of a most two lines.
 * For message spanning multiple lines, see [LatexOutputListenerTest].
 */
class LatexMessageExtractorTest : BasePlatformTestCase() {

    private val currentFile = "test.tex"

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
        val expected = LatexLogMessage("File `paralisy.sty' not found.", currentFile, -1, ERROR)
        testMessageExtractor(text, expected)
    }

    fun testUndefinedControlSequence() {
        val text =
            """./nested/lipsum-one.tex:9: Undefined control sequence.
        l.9 \bloop"""
        val expected = LatexLogMessage("Undefined control sequence. \\bloop", "./nested/lipsum-one.tex", 9, ERROR)
        testMessageExtractor(text, expected)
    }

    fun testPackageError() {
        val text =
            """./errors.tex:8: Package amsmath Error: \begin{split} won't work here."""
        val expected = LatexLogMessage("amsmath: \\begin{split} won't work here.", "./errors.tex", 8, ERROR)
        testMessageExtractor(text, expected)
    }

    fun `test Missing $ inserted`() {
        val text =
            """./main.tex:10: Missing $ inserted.<inserted text>"""
        val expected = LatexLogMessage("Missing $ inserted.", "./main.tex", 10, ERROR)
        testMessageExtractor(text, expected)
    }

    fun `test pdfTeX error cannot open encoding file`() {
        val text = "!pdfTeX error: pdflatex (file texnansi.enc): cannot open encoding file for reading"
        val newText = "ing"
        val expected = LatexLogMessage("pdflatex (file texnansi.enc): cannot open encoding file for reading", "test.tex", -1, ERROR)
        testMessageExtractor(text, expected, newText)
    }

    fun `test The font size command normalsize is not defined`() {
        val text = "./errors.tex:4: LaTeX Error: The font size command \\normalsize is not defined:               there is probably something wrong with the class file."
        val newText = "               there is probably something wrong with the class file."
        val expected = LatexLogMessage("The font size command \\normalsize is not defined: there is probably something wrong with the class file.", "./errors.tex", 4, ERROR)
        testMessageExtractor(text, expected, newText)
    }

    fun `test Misplaced alignment tab character`() {
        val text = "./errors.tex:3: Misplaced alignment tab character &.l.3     &"
        val newText = "l.3     &"
        val expected = LatexLogMessage("Misplaced alignment tab character &.", "./errors.tex", 3, ERROR)
        testMessageExtractor(text, expected, newText)
    }

    fun `test Missing delimiter inserted`() {
        val text = "./errors.tex:4: Missing delimiter (. inserted).<to be read again>"
        val newText = "<to be read again>"
        val expected = LatexLogMessage("Missing delimiter (. inserted).", "./errors.tex", 4, ERROR)
        testMessageExtractor(text, expected, newText)
    }

    fun `test Message with newlines`() {
        val text = "./main.tex:3: Missing \\endcsname inserted.\n" +
            "<to be read again> \n"
        val newText = "<to be read again> \n"
        val expected = LatexLogMessage("Missing \\endcsname inserted.", "./main.tex", 3, ERROR)
        testMessageExtractor(text, expected, newText)
    }

    fun `test begin align ended by end document`() {
        val newText = "t}.\n"
        val text = "./main.tex:10: LaTeX Error: \\begin{align} on input line 4 ended by \\end{documen\n$newText"
        val expected = LatexLogMessage("\\begin{align} on input line 4 ended by \\end{document}.", "./main.tex", 10, ERROR)
        testMessageExtractor(text, expected, newText)
    }

    /*
     * WARNINGS
     */
    fun testPackageWarning() {
        val text = "Package biblatex Warning: Please (re)run Biber on the file:(biblatex) main"
        val expected = LatexLogMessage("biblatex: Please (re)run Biber on the file: main", currentFile, -1, WARNING)
        testMessageExtractor(text, expected)
    }

    fun testUnresolvedReferencesWarning() {
        val text = "LaTeX Warning: There were undefined references."
        val expected = LatexLogMessage("There were undefined references.", currentFile, -1, WARNING)
        testMessageExtractor(text, expected)
    }

    fun testReferenceOnLine() {
        val text = "LaTeX Warning: Reference `fig:bla' on page 1 undefined on input line 10."
        val expected = LatexLogMessage("Reference `fig:bla' undefined", currentFile, 10, WARNING)
        testMessageExtractor(text, expected)
    }

    fun testEndInGroup() {
        val text = "(\\end occurred inside a group at level 1)"
        val expected = LatexLogMessage("\\end occurred inside a group at level 1", currentFile, -1, WARNING)
        testMessageExtractor(text, expected)
    }

    fun testLooseHbox() {
        val text =
            """Loose \hbox (badness 0) in paragraph at lines 9--12
        \OT1/cmr/m/n/10 The badness of this line is 1000.
            """.trimIndent()
        val expected = LatexLogMessage("Loose \\hbox (badness 0) in paragraph at lines 9--12", currentFile, 9, WARNING)
        testMessageExtractor(text, expected)
    }

    fun testFileAlreadyExists() {
        val text = "LaTeX Warning: File `included.tex' already exists on the system. Not generating it from this source."
        val expected = LatexLogMessage("File `included.tex' already exists on the system. Not generating it from this source.", currentFile, -1, WARNING)
        testMessageExtractor(text, expected)
    }

    fun testFontSizeNotAvailable() {
        val text = "LaTeX Font Warning: Font shape `OT1/cmr/m/n' in size <42> not available(Font)              size <24.88> substituted on input line 5."
        val expected = LatexLogMessage("Font shape `OT1/cmr/m/n' in size <42> not available, size <24.88> substituted", currentFile, 5, WARNING)
        testMessageExtractor(text, expected)
    }

    fun testFontShapeUndefined() {
        val text = "LaTeX Font Warning: Font shape `OT1/cmtt/b/n' undefined(Font)              using `OT1/cmtt/m/n' instead on input line 3."
        val expected = LatexLogMessage("Font shape `OT1/cmtt/b/n' undefined, using `OT1/cmtt/m/n' instead", currentFile, 3, WARNING)
        testMessageExtractor(text, expected)
    }

    fun testFileContents() {
        val text = "LaTeX Warning: Writing text `    ' before \\end{filecontents} as last line of filecontents.tex on input line 5."
        val expected = LatexLogMessage("Writing text `    ' before \\end{filecontents} as last line of filecontents.tex", currentFile, 5, WARNING)
        testMessageExtractor(text, expected)
    }

    fun testMissingCharacter() {
        val text = "Missing character: There is no in font !"
        val expected = LatexLogMessage("Missing character: There is no in font !", currentFile, -1, WARNING)
        testMessageExtractor(text, expected)
    }

    fun `test label multiply defined`() {
        val text = "LaTeX Warning: Label `mylabel' multiply defined.)"
        val newText = ")"
        val expected = LatexLogMessage("Label `mylabel' multiply defined.", "test.tex", -1, WARNING)
        testMessageExtractor(text, expected, newText)
    }

    fun `test overfull hbox in paragraph`() {
        val text = "Overfull \\hbox (56.93071pt too wide) in paragraph at lines 13--15\\T1/phv/m/n/10 (-20) leases, cur-rently at [][]$\\T1/cmtt/m/n/10 https : / / dev"
        val newText = "\\T1/phv/m/n/10 (-20) leases, cur-rently at [][]$\\T1/cmtt/m/n/10 https : / / dev"
        val expected = LatexLogMessage("Overfull \\hbox (56.93071pt too wide) in paragraph at lines 13--15", "test.tex", 13, WARNING)
        testMessageExtractor(text, expected, newText)
    }

    fun `test end occurred inside a group`() {
        val text = "(\\end occurred inside a group at level 1)### simple group (level 1) entered at line 4 ({)"
        val newText = "### simple group (level 1) entered at line 4 ({)"
        val expected = LatexLogMessage("(\\end occurred inside a group at level 1)### simple group (level 1) entered at line 4 ({)", "test.tex", 4, WARNING)
        testMessageExtractor(text, expected, newText)
    }

    fun `test end occurred when condition c on line l was incomplete`() {
        val text = "(\\end occurred when \\iftrue on line 4 was incomplete)(\\end occurred when \\ifnum on line 4 was incomplete)"
        val newText = "(\\end occurred when \\ifnum on line 4 was incomplete)"
        val expected = LatexLogMessage("(\\end occurred when \\iftrue on line 4 was incomplete)(\\end occurred when \\ifnum on line 4 was incomplete)", "test.tex", 4, WARNING)
        testMessageExtractor(text, expected, newText)
    }

    fun `test Float specifier changed`() {
        val text = "LaTeX Warning: `h' float specifier changed to `ht'.\n"
        val newText = "\n"
        val expected = LatexLogMessage("`h' float specifier changed to `ht'.", "test.tex", -1, WARNING)
        testMessageExtractor(text, expected, newText)
    }

    fun `test Missing character`() {
        val text = "Missing character: There is no ^^A in font [lmroman10-regular]:mapping=tex-text;!"
        val newText = ";!"
        val expected = LatexLogMessage("Missing character: There is no ^^A in font [lmroman10-regular]:mapping=tex-text;!", "test.tex", -1, WARNING)
        testMessageExtractor(text, expected, newText)
    }

    fun `test You have requested package`() {
        val text = "LaTeX Warning: You have requested package `',               but the package provides `mypackage'."
        val newText = "               but the package provides `mypackage'."
        val expected = LatexLogMessage("You have requested package `', but the package provides `mypackage'.", "test.tex", -1, WARNING)
        testMessageExtractor(text, expected, newText)
    }

    fun `test unused global option`() {
        val newText = "    [a4]."
        val text = "LaTeX Warning: Unused global option(s):$newText"
        val expected = LatexLogMessage("Unused global option(s): [a4].", "test.tex", -1, WARNING)
        testMessageExtractor(text, expected, newText)
    }

    /**
     * @param text Line 1 joined with line 2 (line 2 can be empty)
     * @param newText Line 2
     */
    private fun testMessageExtractor(text: String, expected: LatexLogMessage?, newText: String = "") {
        val real = LatexLogMessageExtractor.findMessage(text.removeAll("\n", "\r"), newText.removeAll("\n"), currentFile)
        assertEquals(expected, real)
    }
}
