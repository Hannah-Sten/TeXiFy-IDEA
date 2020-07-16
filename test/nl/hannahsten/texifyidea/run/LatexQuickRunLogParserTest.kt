package nl.hannahsten.texifyidea.run

import com.intellij.testFramework.fixtures.BasePlatformTestCase
import nl.hannahsten.texifyidea.run.bibtex.logtab.BibtexLogMessage
import nl.hannahsten.texifyidea.run.latex.logtab.LatexLogMessage
import nl.hannahsten.texifyidea.run.latex.logtab.LatexOutputListener
import nl.hannahsten.texifyidea.run.latex.logtab.ui.LatexCompileMessageTreeView

class LatexQuickRunLogParserTest : BasePlatformTestCase() {
    /**
     * Useful regex for matching braces: \(([^(^)]+)\)
     */

    override fun getTestDataPath(): String {
        return "test/resources/run"
    }

    fun testFileStack() {
        val text = """
        ! I can't find file `ecrm1000'.
        <*> ...ljfour; mag:=1; nonstopmode; input ecrm1000
    """.trimIndent()

        println(runLogParser(text))
    }

    private fun runLogParser(inputText: String): List<LatexLogMessage> {
        val srcRoot = myFixture.copyDirectoryToProject("./", "./")
        val project = myFixture.project
        val mainFile = srcRoot.findFileByRelativePath("main.tex")
        val latexMessageList = mutableListOf<LatexLogMessage>()
        val bibtexMessageList = mutableListOf<BibtexLogMessage>()
        val treeView =
            LatexCompileMessageTreeView(project)
        val listener = LatexOutputListener(project, mainFile, latexMessageList, bibtexMessageList, treeView)

        val input = inputText.split('\n')
        input.forEach { listener.processNewText(it) }

        return latexMessageList.toList()
    }
}