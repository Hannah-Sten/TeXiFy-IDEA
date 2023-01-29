package nl.hannahsten.texifyidea.remotelibraries

import nl.hannahsten.texifyidea.psi.BibtexEntry
import nl.hannahsten.texifyidea.remotelibraries.state.BibtexEntryListConverter
import nl.hannahsten.texifyidea.ui.remotelibraries.LibraryMutableTreeNode

class LocalBibLibrary(override val identifier: String = "local", override val displayName: String = "local") : ExternalBibLibrary(identifier, displayName) {

    /**
     * TODO Idea: store entries from local file in (its own?) index and get the items from there to show them in the
     *   library tool window. Get entries for autocompletion in the same way as we get them from the regular/existing index.
     *
     * TODO if possible, do we automatically reflect changes in bib file in tree?
     */
    private fun getCollection(): List<BibtexEntry> = BibtexEntryListConverter().fromString(
        """
            @BOOK{Parker2020-bu,
              title     = "Humble pi",
              author    = "Parker, Matt",
              publisher = "Penguin Books",
              month     =  mar,
              year      =  2020,
              address   = "Harlow, England"
            }
        """.trimIndent()
    )

    fun asTreeNode() = LibraryMutableTreeNode(identifier, displayName, getCollection())
}