package nl.hannahsten.texifyidea.reference

import com.intellij.codeInsight.completion.CompletionType
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockkObject
import io.mockk.unmockkAll
import nl.hannahsten.texifyidea.configureByFilesAndBuildFilesets
import nl.hannahsten.texifyidea.remotelibraries.RemoteLibraryManager
import nl.hannahsten.texifyidea.remotelibraries.state.BibtexEntryListConverter
import nl.hannahsten.texifyidea.remotelibraries.state.LibraryState
import nl.hannahsten.texifyidea.remotelibraries.zotero.ZoteroLibrary

class BibtexIdRemoteLibraryCompletionTest : BasePlatformTestCase() {

    override fun getTestDataPath(): String = "test/resources/completion/cite/library"

    @Throws(Exception::class)
    override fun setUp() {
        super.setUp()
    }

    // TODO(TEX-213) Fix tests using file set cache
    /**
     * Complete item from remote bib, and add it to the bib file.
     */
    fun testCiteFromLibraryCompletionWithBib() {
        completionWithRemoteBib(
            """
            @book{gardner_knots_2014,
                address = {New York : Washington, DC},
                edition = {New edition},
                series = {The new {Martin} {Gardner} mathematical library},
                title = {Knots and borromean rings, rep-tiles, and eight queens: {Martin} {Gardner}'s unexpected hanging},
                isbn = {9780521756136 9780521758710},
                shorttitle = {Knots and borromean rings, rep-tiles, and eight queens},
                abstract = {"The hangman's paradox, cat's cradle, gambling, peg solitaire, pi and e - all these and more are back in Martin Gardner's inimitable style, with updates on new developments and discoveries. Read about how knots and molecules are related; take a trip into the fourth dimension; try out new dissections of stars, crosses, and polygons; and challenge yourself with new twists on classic games"--},
                number = {4},
                publisher = {Cambridge University Press ; Mathematical Association of America},
                author = {Gardner, Martin},
                year = {2014},
                keywords = {MATHEMATICS / General, Mathematical recreations},
            }
            """.trimIndent()
        )
    }

    /**
     * Complete item from remote bib, and add it to the currently empty bib file.
     */
    fun testCiteFromLibraryCompletion() {
        completionWithRemoteBib(
            """
            @book{newey_how_2017,
                address = {London},
                title = {How to build a car},
                isbn = {9780008196806},
                language = {eng},
                publisher = {HarperCollins Publishers},
                author = {Newey, Adrian},
                year = {2017},
            }
            """.trimIndent()
        )
    }

    /**
     * The to be completed item that is in the remote bib is also in the local bib already. The item should be completed
     * from the local bib (we can't check this) and it should not be added to the local bib again (this is what we check).
     */
    fun testCiteFromLibraryAlreadyLocal() {
        completionWithRemoteBib(
            """
            @book{newey_how_2017,
                address = {London},
                title = {How to build a car},
                isbn = {9780008196806},
                language = {eng},
                publisher = {HarperCollins Publishers},
                author = {Newey, Adrian},
                year = {2017},
            }
            """.trimIndent()
        )
    }

    /**
     * For each test that uses this function, create a folder <test name> in `test/resources/completion/cite/library`
     * with the following four files:
     *
     * - before.tex: Tex file that contains `\cite{key<caret>}`. It is important that part of the to-be-completed key is
     *   already there, and that there is only one completion candidate, so it will be completed automatically.
     * - after.tex
     * - bibtex_before.tex: The local bib file.
     * - bibtex_after.tex: The bibtex file with the completed bib entry added, if the entry was only in the remote bib before completion.
     *
     * and call this function with the bib string from the remote library as the argument.
     */
    private fun completionWithRemoteBib(remoteBib: String) {
        try {
            val path = getTestName(false)

            mockkObject(RemoteLibraryManager)
            every { RemoteLibraryManager.getInstance().getLibraries() } returns mutableMapOf("aaa" to LibraryState("mocked", ZoteroLibrary::class.java, BibtexEntryListConverter().fromString(remoteBib), "test url"))

            myFixture.configureByFilesAndBuildFilesets("$path/before.tex", "$path/bibtex_before.bib")

            myFixture.complete(CompletionType.BASIC)
            // TODO(TEX-213) Fix tests using file set cache
            myFixture.checkResultByFile("$path/before.tex", "$path/after.tex", true)
            myFixture.checkResultByFile("$path/bibtex_before.bib", "$path/bibtex_after.bib", true)
        }
        finally {
            clearAllMocks()
            unmockkAll()
        }
    }
}