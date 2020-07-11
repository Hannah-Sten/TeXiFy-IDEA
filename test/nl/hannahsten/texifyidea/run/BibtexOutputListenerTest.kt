package nl.hannahsten.texifyidea.run

import com.intellij.testFramework.fixtures.BasePlatformTestCase
import nl.hannahsten.texifyidea.run.bibtex.logtab.BibtexLogMessage
import nl.hannahsten.texifyidea.run.bibtex.logtab.BibtexLogMessageType.ERROR
import nl.hannahsten.texifyidea.run.bibtex.logtab.BibtexOutputListener
import nl.hannahsten.texifyidea.run.latex.ui.LatexCompileMessageTreeView

class BibtexOutputListenerTest : BasePlatformTestCase() {

    override fun getTestDataPath(): String {
        return "test/resources/run"
    }

    private fun testLog(log: String, expectedMessages: Set<BibtexLogMessage>) {
        val srcRoot = myFixture.copyDirectoryToProject("./", "./")
        val project = myFixture.project
        val mainFile = srcRoot.findFileByRelativePath("main.tex")
        val bibtexMessageList = mutableListOf<BibtexLogMessage>()
        val treeView = LatexCompileMessageTreeView(project)
        val listener = BibtexOutputListener(project, mainFile, bibtexMessageList, treeView)

        val input = log.split('\n')
        input.forEach { listener.processNewText(it) }

        assertEquals(expectedMessages, bibtexMessageList.toSet())
    }

    fun `test I couldn't open Database file`() {
        val log = """
            This is BibTeX, Version 0.99d (TeX Live 2020)
            The top-level auxiliary file: bibtex-mwe.aux
            I couldn't open database file references34.bib
            ---line 3 of file bibtex-mwe.aux
             : \bibdata{references34
             :                      }
            I'm skipping whatever remains of this command
            The style file: plain.bst
            I found no database files---while reading file bibtex-mwe.aux
            Warning--I didn't find a database entry for "knuth19902"
            (There were 2 error messages)

            Process finished with exit code 2
        """.trimIndent()

        val expectedMessages = setOf(
            BibtexLogMessage("I couldn't open database file references34.bib", "bibtex-mwe.aux", 3, ERROR)
        )

        testLog(log, expectedMessages)
    }

    fun `test Sorry---you've exceeded BibTeX's`() {
        val log = """
            Database file #3: crypto.bib
            Sorry---you've exceeded BibTeX's hash size 100000
            Aborted at line 291526 of file crypto.bib
            (That was a fatal error)
        """.trimIndent()

        val expectedMessages = setOf(
            BibtexLogMessage("Sorry---you've exceeded BibTeX's hash size 100000", "crypto.bib", 291526, ERROR)
        )

        testLog(log, expectedMessages)
    }

    fun `test Illegal, another bibdata command`() {
        val log = """
            A level-1 auxiliary file: chapter2.aux
            Illegal, another \bibdata command---line 4 of file chapter2.aux
             : \bibdata
             :         {references}
            I'm skipping whatever remains of this command
            Database file #1: references.bib
        """.trimIndent()

        val expectedMessages = setOf(
            BibtexLogMessage("Illegal, another \\bibdata command", "chapter2.aux", 4, ERROR)
        )

        testLog(log, expectedMessages)
    }
}