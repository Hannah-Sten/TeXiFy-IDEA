package nl.hannahsten.texifyidea.run

import com.intellij.testFramework.fixtures.BasePlatformTestCase
import nl.hannahsten.texifyidea.run.latex.ui.LatexCompileMessageTreeView
import nl.hannahsten.texifyidea.run.latex.ui.LatexOutputListener
import nl.hannahsten.texifyidea.run.latex.ui.LatexOutputListener.LatexLogMessage
import nl.hannahsten.texifyidea.run.latex.ui.LatexOutputListener.LatexLogMessageType.ERROR
import nl.hannahsten.texifyidea.run.latex.ui.LatexOutputListener.LatexLogMessageType.WARNING

class LatexMessageExtractorTest : BasePlatformTestCase() {
    fun testEnvironmentUndefinedError() {
        val text = "./main.tex:1: LaTeX Error: Environment align undefined."
        val expected = LatexLogMessage("LaTeX Error: Environment align undefined.", "./main.tex", 0, ERROR)
        testMessageExtractor(text, expected)
    }

    fun testUndefinedControlSequenceInNestedFileError() {
        val text = "./nested/lipsum-one.tex:9: Undefined control sequence."
        val expected = LatexLogMessage("Undefined control sequence.", "./nested/lipsum-one.tex", 8, ERROR)
        testMessageExtractor(text, expected)
    }

    fun testPackageNotInstalledError() {
        val text = "! LaTeX Error: File `paralisy.sty' not found."
        val expected = LatexLogMessage("! LaTeX Error: File `paralisy.sty' not found.", null, null, WARNING)
        testMessageExtractor(text, expected)
    }

    fun testPackageWarning() {
        val text = "Package biblatex Warning: Please (re)run Biber on the file:"
        val expected = LatexLogMessage("Package biblatex Warning: Please (re)run Biber on the file:", null, null, WARNING)
        testMessageExtractor(text, expected)
    }

    fun testUnresolvedReferencesWarning() {
        val text = "LaTeX Warning: There were undefined references."
        val expected = LatexLogMessage("LaTeX Warning: There were undefined references.", null, null, WARNING)
        testMessageExtractor(text, expected)
    }

    private fun testMessageExtractor(text: String, expected: LatexLogMessage) {
        val listModel = mutableListOf<LatexLogMessage>()
        val treeView = LatexCompileMessageTreeView(project)
        val listener = LatexOutputListener(project, null, listModel, treeView)

        val real = listener.findMessage(text, "") ?: return fail()

        assertEquals(expected, real)
    }
}