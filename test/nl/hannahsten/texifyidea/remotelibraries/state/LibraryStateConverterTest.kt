package nl.hannahsten.texifyidea.remotelibraries.state

import com.intellij.testFramework.fixtures.BasePlatformTestCase
import nl.hannahsten.texifyidea.file.BibtexFileType
import nl.hannahsten.texifyidea.psi.BibtexEntry
import nl.hannahsten.texifyidea.remotelibraries.zotero.ZoteroLibrary
import nl.hannahsten.texifyidea.testutils.toSystemNewLine
import nl.hannahsten.texifyidea.util.parser.collectSubtreeTyped

class LibraryStateConverterTest : BasePlatformTestCase() {

    val identifier = "a1234"

    val displayName = "Zotero test"

    val bibString = """
         @Article{greenwade1993,
                author = "George D. Greenwade",
            }

            @book{newey_how_2017,
                title = {How to build a car},
            }
    """.trimIndent()

    val xmlString = """
        <LinkedHashMap>
          <a1234>
            <displayName>Zotero test</displayName>
            <libraryType>nl.hannahsten.texifyidea.remotelibraries.zotero.ZoteroLibrary</libraryType>
            <bibtex>@Article{greenwade1993,
               author = "George D. Greenwade",
           }
        @book{newey_how_2017,
               title = {How to build a car},
           }</bibtex>
            <url>test url</url>
          </a1234>
        </LinkedHashMap>
        
    """.trimIndent()

    fun testToString() {
        myFixture.configureByText(BibtexFileType, bibString)

        val entries: List<BibtexEntry> = myFixture.file.collectSubtreeTyped<BibtexEntry>().toList()

        val result = LibraryStateConverter().toString(
            mapOf(
                identifier to LibraryState(
                    displayName,
                    ZoteroLibrary::class.java,
                    entries,
                    "test url"
                )
            )
        )

        assertEquals(xmlString.toSystemNewLine(), result?.toSystemNewLine())
    }

    fun testFromString() {
        myFixture.configureByText(BibtexFileType, bibString)

        val entries: List<BibtexEntry> = myFixture.file.collectSubtreeTyped<BibtexEntry>().toList()

        val result = LibraryStateConverter().fromString(xmlString)

        assertEquals(mapOf(identifier to LibraryState(displayName, ZoteroLibrary::class.java, entries, "test url")).toString().trim(), result.toString().trim())
    }
}