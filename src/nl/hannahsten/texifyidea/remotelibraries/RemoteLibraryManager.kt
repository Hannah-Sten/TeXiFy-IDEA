package nl.hannahsten.texifyidea.remotelibraries

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import nl.hannahsten.texifyidea.psi.BibtexEntry
import nl.hannahsten.texifyidea.remotelibraries.state.RemoteLibraryState

@State(name = "ExternalBibLibraryManager", storages = [(Storage("library.xml"))])
class RemoteLibraryManager : PersistentStateComponent<RemoteLibraryState> {

    companion object {

        fun getInstance(): RemoteLibraryManager = ApplicationManager.getApplication().getService(RemoteLibraryManager::class.java)
    }

    var libraries: MutableMap<String, List<BibtexEntry>> = mutableMapOf()

    override fun getState(): RemoteLibraryState {
        return RemoteLibraryState(libraries.toMap())
    }

    override fun loadState(state: RemoteLibraryState) {
        libraries = state.libraries.toMutableMap()
    }

    fun updateLibrary(library: RemoteBibLibrary, bibItems: List<BibtexEntry>) {
        libraries[library.name] = bibItems
    }

    fun removeLibrary(library: RemoteBibLibrary) {
        removeLibraryByKey(library.name)
    }

    fun removeLibraryByKey(key: String) {
        libraries.remove(key)
    }
}