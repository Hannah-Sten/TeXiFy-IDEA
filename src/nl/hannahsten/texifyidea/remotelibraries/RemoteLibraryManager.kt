package nl.hannahsten.texifyidea.remotelibraries

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import nl.hannahsten.texifyidea.psi.BibtexEntry
import nl.hannahsten.texifyidea.remotelibraries.state.LibraryState
import nl.hannahsten.texifyidea.remotelibraries.state.RemoteLibraryState

@State(name = "ExternalBibLibraryManager", storages = [(Storage("library.xml"))])
class RemoteLibraryManager : PersistentStateComponent<RemoteLibraryState> {

    companion object {

        fun getInstance(): RemoteLibraryManager = ApplicationManager.getApplication().getService(RemoteLibraryManager::class.java)
    }

    var libraries: MutableMap<String, LibraryState> = mutableMapOf()

    override fun getState(): RemoteLibraryState {
        return RemoteLibraryState(libraries.toMap())
    }

    override fun loadState(state: RemoteLibraryState) {
        libraries = state.libraries.toMutableMap()
    }

    /**
     * Update the stored bib entries if the library already has an entry, otherwise create a new entry.
     */
    fun updateLibrary(library: RemoteBibLibrary, bibItems: List<BibtexEntry>) {
        libraries[library.identifier]?.let {
            it.entries = bibItems
        } ?: run {
            libraries[library.identifier] = LibraryState(library.displayName, library::class.java, bibItems)
        }
    }

    fun removeLibrary(library: RemoteBibLibrary) {
        removeLibraryByKey(library.identifier)
    }

    fun removeLibraryByKey(key: String) {
        libraries.remove(key)
    }
}