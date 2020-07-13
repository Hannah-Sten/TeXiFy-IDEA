package nl.hannahsten.texifyidea.run

import com.intellij.testFramework.fixtures.BasePlatformTestCase
import nl.hannahsten.texifyidea.run.bibtex.logtab.BibtexLogMessage
import nl.hannahsten.texifyidea.run.bibtex.logtab.BibtexLogMessageType.ERROR
import nl.hannahsten.texifyidea.run.bibtex.logtab.BibtexLogMessageType.WARNING
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

    /*
     * Errors
     */

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
            BibtexLogMessage("I couldn't open database file references34.bib", "bibtex-mwe.aux", 3, ERROR),
            BibtexLogMessage("I found no database files", "bibtex-mwe.aux", null, ERROR),
            BibtexLogMessage("I didn't find a database entry for \"knuth19902\"", "", null, WARNING)
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

    fun `test I found no type while reading file`() {
        val log = """
            I found no \bibdata command---while reading file bibtex-mwe.aux
        """.trimIndent()

        val expectedMessages = setOf(
            BibtexLogMessage("I found no \\bibdata command", "bibtex-mwe.aux", null, ERROR)
        )

        testLog(log, expectedMessages)
    }

    fun `test A bad cross reference refers to entry`() {
        val log = """
            Database file #1: references.bib
            A bad cross reference---entry "knuth1990"
            refers to entry "nothing", which doesn't exist
            Warning--I didn't find a database entry for "nothing"
            (There was 1 error message)
            
            Process finished with exit code 2
        """.trimIndent()

        val expectedMessages = setOf(
            BibtexLogMessage("A bad cross reference---entry \"knuth1990\" refers to entry \"nothing\", which doesn't exist", "references.bib", null, ERROR),
            BibtexLogMessage("I didn't find a database entry for \"nothing\"", "references.bib", null, WARNING)
        )

        testLog(log, expectedMessages)
    }

    fun `test Too many commas in name`() {
        val log = """
            Database file #1: references.bib
            Too many commas in name 1 of "D.E. Knuth, D.E. Knuth, D.E. Knuth, D.E. Knuth, D.E. Knuth, D.E. Knuth, D.E. Knuth" for entry knuth1990
            while executing---line 1049 of file plain.bst
        """.trimIndent()

        val expectedMessages = setOf(
            BibtexLogMessage("Too many commas in name 1 of \"D.E. Knuth, D.E. Knuth, D.E. Knuth, D.E. Knuth, D.E. Knuth, D.E. Knuth, D.E. Knuth\" for entry knuth1990", "references.bib", null, ERROR)
        )

        testLog(log, expectedMessages)
    }

    /*
     * Warnings
     */

    fun `test I'm ignoring extra field`() {
        val log = """
            Database file #1: references.bib
            Warning--I'm ignoring knuth1990's extra "author" field
            --line 5 of file references.bib
            (There was 1 warning)
        """.trimIndent()

        val expectedMessages = setOf(
            BibtexLogMessage("I'm ignoring knuth1990's extra \"author\" field", "references.bib", 5, WARNING)
        )

        testLog(log, expectedMessages)
    }

    fun `test I didn't find a database entry`() {
        val log = """
            Database file #1: references.bib
            Warning--I didn't find a database entry for "knuth19902"
            (There was 1 warning)

            Process finished with exit code 2
        """.trimIndent()

        val expectedMessages = setOf(
            BibtexLogMessage("I didn't find a database entry for \"knuth19902\"", "references.bib", null, WARNING)
        )

        testLog(log, expectedMessages)
    }

    fun `test isn't a brace-balanced string for entry`() {
        val log = """
            Database file #1: mybib.bib
            Warning--"{" isn't a brace-balanced string for entry BTRAF
            while executing--line 939 of file prsty.bst
            
            Process finished with exit code 2
        """.trimIndent()

        val expectedMessages = setOf(
            BibtexLogMessage("\"{\" isn't a brace-balanced string for entry BTRAF", "mybib.bib", null, WARNING)
        )

        testLog(log, expectedMessages)
    }


}