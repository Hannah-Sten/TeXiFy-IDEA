package nl.hannahsten.texifyidea.run

import com.intellij.testFramework.fixtures.BasePlatformTestCase
import nl.hannahsten.texifyidea.run.latex.logtab.LatexLogMessage
import nl.hannahsten.texifyidea.run.latex.logtab.LatexLogMessageType.WARNING
import nl.hannahsten.texifyidea.run.latex.logtab.LatexOutputListener
import nl.hannahsten.texifyidea.run.latex.ui.LatexCompileMessageTreeView

class LatexFileStackTest : BasePlatformTestCase() {


    override fun getTestDataPath(): String {
        return "test/resources/run"
    }

    fun testFileStack() {
        val TEST_LOG = """
(./main.tex
LaTeX2e <2020-02-02> patch level 5
L3 programming layer <2020-03-06> (./lipsum.tex
Document Class: test/test 2019/12/19 test class
 (./math.tex)
Package bla Warning: I don't know where I belong
     
) 
)
    """.trimIndent()

        val messages = runLogParser(TEST_LOG)
        val expected = listOf(LatexLogMessage("bla: I don't know where I belong", "lipsum.tex", 0, WARNING))
        assertEquals(expected, messages)
    }

    private fun runLogParser(inputText: String): List<LatexLogMessage> {
        val srcRoot = myFixture.copyDirectoryToProject("./", "./")
        val project = myFixture.project
        val mainFile = srcRoot.findFileByRelativePath("main.tex")
        val latexMessageList = mutableListOf<LatexLogMessage>()
        val bibtexMessageList = mutableListOf<LatexLogMessage>()
        val treeView = LatexCompileMessageTreeView(project)
        val listener = LatexOutputListener(project, mainFile, latexMessageList, bibtexMessageList, treeView)

        val input = inputText.split('\n')
        input.forEach { listener.processNewText(it) }

        return latexMessageList.toList()
    }
}