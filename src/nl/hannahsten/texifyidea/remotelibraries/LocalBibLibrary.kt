package nl.hannahsten.texifyidea.remotelibraries

import com.intellij.openapi.project.Project
import nl.hannahsten.texifyidea.index.file.BibtexExternalEntryIndex
import nl.hannahsten.texifyidea.psi.BibtexEntry
import nl.hannahsten.texifyidea.ui.remotelibraries.LibraryMutableTreeNode

class LocalBibLibrary(override val identifier: String = "local", override val displayName: String = "local") : ExternalBibLibrary(identifier, displayName) {

    /**
     * TODO Idea: store entries from local file in (its own?) index and get the items from there to show them in the
     *   library tool window. Get entries for autocompletion in the same way as we get them from the regular/existing index.
     *
     * TODO if possible, do we automatically reflect changes in bib file in tree?
     */
    private fun getCollection(project: Project): List<BibtexEntry> = BibtexExternalEntryIndex.getAllValues(project).toList()

    fun asTreeNode(project: Project) = LibraryMutableTreeNode(identifier, displayName, getCollection(project))
}