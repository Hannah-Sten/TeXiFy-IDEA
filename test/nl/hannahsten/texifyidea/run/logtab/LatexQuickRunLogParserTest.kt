package nl.hannahsten.texifyidea.run.logtab

import com.intellij.testFramework.fixtures.BasePlatformTestCase
import nl.hannahsten.texifyidea.run.bibtex.logtab.BibtexLogMessage
import nl.hannahsten.texifyidea.run.latex.logtab.LatexLogMessage
import nl.hannahsten.texifyidea.run.latex.logtab.LatexOutputListener
import nl.hannahsten.texifyidea.run.latex.logtab.ui.LatexCompileMessageTreeView

class LatexQuickRunLogParserTest : BasePlatformTestCase() {

    /**
     * Useful regex for matching braces: \(([^(^)]+)\)
     */

    override fun getTestDataPath(): String = "test/resources/run"

    fun testFileStack() {
        val text =
            """
(main.toc
Underfull \vbox (badness 3449) has occurred while \output is active []

 [25
            """.trimIndent()

        runLogParser(text).forEach {
            println(it)
            println()
        }
    }

    private fun runLogParser(inputText: String): List<LatexLogMessage> {
        val srcRoot = myFixture.copyDirectoryToProject("./", "./")
        val project = myFixture.project
        val mainFile = srcRoot.findFileByRelativePath("main.tex")
        val latexMessageList = mutableListOf<LatexLogMessage>()
        val bibtexMessageList = mutableListOf<BibtexLogMessage>()
        val treeView = LatexCompileMessageTreeView(project, latexMessageList, bibtexMessageList)
        val listener = LatexOutputListener(project, mainFile, latexMessageList, bibtexMessageList, treeView)

        val input = inputText.split('\n')
        input.forEach { listener.processNewText(it) }

        return latexMessageList.toList()
    }
}