package nl.hannahsten.texifyidea.externallibrary

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import nl.hannahsten.texifyidea.psi.BibtexEntry

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
}