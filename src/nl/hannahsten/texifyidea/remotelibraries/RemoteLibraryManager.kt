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

    private var libraries: MutableMap<String, LibraryState> = mutableMapOf()

    fun getLibraries(): Map<String, LibraryState> = libraries.toMap()

    override fun getState(): RemoteLibraryState = RemoteLibraryState(libraries.toMap())

    override fun loadState(state: RemoteLibraryState) {
        libraries = state.libraries.toMutableMap()
    }

    /**
     * Update the stored bib entries if the library already has an entry, otherwise create a new entry.
     */
    fun updateLibrary(library: RemoteBibLibrary, bibItems: List<BibtexEntry>, url: String? = null) {
        libraries[library.identifier]?.let {
            it.entries = bibItems
        } ?: run {
            libraries[library.identifier] = LibraryState(library.displayName, library::class.java, bibItems, url)
        }
    }

    fun removeLibraryByKey(key: String) {
        libraries.remove(key)
    }
}