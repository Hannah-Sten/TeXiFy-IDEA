package nl.hannahsten.texifyidea.run.logtab

import com.intellij.testFramework.fixtures.BasePlatformTestCase
import nl.hannahsten.texifyidea.run.legacy.bibtex.logtab.BibtexLogMessage
import nl.hannahsten.texifyidea.run.legacy.bibtex.logtab.BibtexLogMessageType.ERROR
import nl.hannahsten.texifyidea.run.legacy.bibtex.logtab.BibtexLogMessageType.WARNING
import nl.hannahsten.texifyidea.run.legacy.bibtex.logtab.BibtexOutputListener

class BibtexOutputListenerTest : BasePlatformTestCase() {

    override fun getTestDataPath(): String {
        return "test/resources/run"
    }

    private fun testLog(log: String, expectedMessages: Set<BibtexLogMessage>) {
        val srcRoot = myFixture.copyDirectoryToProject("./", "./")
        val project = myFixture.project
        val mainFile = srcRoot.findFileByRelativePath("main.tex")
        val bibtexMessageList = mutableListOf<BibtexLogMessage>()
        val listener = BibtexOutputListener(project, mainFile, bibtexMessageList)

        val input = log.split('\n')
        input.forEach { listener.processNewText(it) }

        assertEquals(expectedMessages, bibtexMessageList.toSet())
    }

    /*
     * Errors
     */

    fun `test I couldn't open Database file`() {
        val log =
            """
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
        val log =
            """
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
        val log =
            """
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
        val log =
            """
            I found no \bibdata command---while reading file bibtex-mwe.aux
            """.trimIndent()

        val expectedMessages = setOf(
            BibtexLogMessage("I found no \\bibdata command", "bibtex-mwe.aux", null, ERROR)
        )

        testLog(log, expectedMessages)
    }

    fun `test A bad cross reference refers to entry`() {
        val log =
            """
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
        val log =
            """
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
        val log =
            """
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
        val log =
            """
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
        val log =
            """
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

    fun `test I didn't find any fields`() {
        val log =
            """
            The style file: style.bst
            Warning--I didn't find any fields--line 1 of file style.bst
            (There was 1 warning)
            
            Process finished with exit code 0
            """.trimIndent()

        val expectedMessages = setOf(
            BibtexLogMessage("I didn't find any fields", "style.bst", 1, WARNING)
        )

        testLog(log, expectedMessages)
    }

    fun `test You've nested cross references`() {
        val log =
            """
            Database file #1: references.bib
            Warning--you've nested cross references--entry "knuth1990"
            refers to entry "greenwade1993", which also refers to something
            Warning--can't use both volume and number fields in knuth1990
            (There were 2 warnings)
            
            Process finished with exit code 0
            """.trimIndent()

        val expectedMessages = setOf(
            BibtexLogMessage("you've nested cross references--entry \"knuth1990\" refers to entry \"greenwade1993\", which also refers to something", "references.bib", null, WARNING),
            BibtexLogMessage("can't use both volume and number fields in knuth1990", "references.bib", null, WARNING)
        )

        testLog(log, expectedMessages)
    }

    /*
     * Biber
     */

    fun `test cannot find file`() {
        val log =
            """
            INFO - Globbed data source 'references2.bib' to references2.bib
            INFO - Looking for bibtex format file 'references2.bib' for section 0
            ERROR - Cannot find 'references2.bib'!
            INFO - ERRORS: 1
            """.trimIndent()

        val expectedMessages = setOf(
            BibtexLogMessage("Cannot find 'references2.bib'!", "", null, ERROR)
        )

        testLog(log, expectedMessages)
    }

    fun `test syntax error`() {
        val log =
            """
            INFO - Looking for bibtex format file 'references.bib' for section 0
            INFO - LaTeX decoding ...
            INFO - Found BibTeX data source '/home/thomas/stuffs/src/references.bib'
            WARN - BibTeX subsystem: /tmp/biber_tmp_lucp/references.bib_402841.utf8, line 12, warning: possible runaway string started at line 11
            ERROR - BibTeX subsystem: /tmp/biber_tmp_lucp/references.bib_402841.utf8, line 20, syntax error: found "{greenwade1993,     author  = "George D. Greenwade",     title   = "The {C}omprehensive {T}ex {A}rchive {N}etwork ({CTAN})",     year    = "1993",     journal = "TUGBoat",     volume  = "14",     number  = "3",     pages   = "342--351",     note    = mytext, }", expected "="
            INFO - WARNINGS: 1
            INFO - ERRORS: 1
            
            Process finished with exit code 2
            """.trimIndent()

        val expectedMessages = setOf(
            BibtexLogMessage("possible runaway string started at line 11", "/home/thomas/stuffs/src/references.bib", 12, WARNING),
            BibtexLogMessage("syntax error: found \"{greenwade1993,     author  = \"George D. Greenwade\",     title   = \"The {C}omprehensive {T}ex {A}rchive {N}etwork ({CTAN})\",     year    = \"1993\",     journal = \"TUGBoat\",     volume  = \"14\",     number  = \"3\",     pages   = \"342--351\",     note    = mytext, }\", expected \"=\"", "/home/thomas/stuffs/src/references.bib", 20, ERROR)
        )

        testLog(log, expectedMessages)
    }

    fun `test warning in file`() {
        val log =
            """
            INFO - Found BibTeX data source 'references.bib'
            WARN - Invalid or undefined BibTeX entry key in file '/tmp/biber_tmp_lucp/references.bib_402841.utf8', skipping ...
            """.trimIndent()

        val expectedMessages = setOf(
            BibtexLogMessage("Invalid or undefined BibTeX entry key", "references.bib", null, WARNING)
        )

        testLog(log, expectedMessages)
    }

    fun `test long biber warning`() {
        val log =
            """
            INFO - Globbed data source 'references.bib' to references.bib
            INFO - Looking for bibtex format file 'references.bib' for section 0
            INFO - LaTeX decoding ...
            INFO - Found BibTeX data source 'references.bib'
            WARN - Possible typo (case mismatch) between citation and datasource keys: 'blablablaS' and 'blablablas' in file 'references.bib'
            WARN - I didn't find a database entry for 'blablablaS' (section 0)
            INFO - Overriding locale 'en-GB' defaults 'variable = shifted' with 'variable = non-ignorable'
            INFO - Overriding locale 'en-GB' defaults 'normalization = NFD' with 'normalization = prenormalized'
            INFO - Sorting list 'nty/global//global/global' of type 'entry' with template 'nty' and locale 'en-GB'
            """.trimIndent()

        val expectedMessages = setOf(
            BibtexLogMessage("Possible typo (case mismatch) between citation and datasource keys: 'blablablaS' and 'blablablas' in file 'references.bib'", "references.bib", null, WARNING),
            BibtexLogMessage("I didn't find a database entry for 'blablablaS' (section 0)", "references.bib", null, WARNING)
        )

        testLog(log, expectedMessages)
    }
}