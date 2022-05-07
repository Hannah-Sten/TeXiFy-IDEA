package nl.hannahsten.texifyidea.externallibrary

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import nl.hannahsten.texifyidea.psi.BibtexEntry

@State(name = "ExternalBibLibraryManager", storages = [(Storage("library.xml"))])
class ExternalLibraryManager : PersistentStateComponent<ExternalLibraryState> {

    companion object {

        fun getInstance(): ExternalLibraryManager = ApplicationManager.getApplication().getService(ExternalLibraryManager::class.java)
    }

    var libraries: MutableMap<String, List<BibtexEntry>> = mutableMapOf()

    override fun getState(): ExternalLibraryState {
        return ExternalLibraryState(libraries.toMap())
    }

    override fun loadState(state: ExternalLibraryState) {
        libraries = state.libraries.toMutableMap()
    }

    fun updateLibrary(library: ExternalBibLibrary, bibItems: List<BibtexEntry>) {
        libraries[library.name] = bibItems
    }
}