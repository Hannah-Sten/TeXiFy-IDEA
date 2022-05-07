package nl.hannahsten.texifyidea.externallibrary

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import com.intellij.util.xmlb.annotations.OptionTag
import nl.hannahsten.texifyidea.psi.BibtexEntry

@State(name = "ExternalBibLibraryManager", storages = [(Storage("library.xml"))])
class ExternalLibraryManager : PersistentStateComponent<ExternalLibraryState> {


    companion object {
        fun getInstance(): ExternalLibraryManager = ApplicationManager.getApplication().getService(ExternalLibraryManager::class.java)
    }
//    data class ExternalLibraryState(var libraries: Map<String, LibraryItems>)

    data class LibraryItems(
        val items: List<BibtexEntry> = emptyList()
    )

//    @JvmField
//    @OptionTag(converter = BibtexEntryListConverter::class)
//    var libraries: MutableMap<String, List<BibtexEntry>> = mutableMapOf()
    var libraries: Boolean = false

    override fun getState(): ExternalLibraryState {
//        return ExternalLibraryState(libraries.mapValues { LibraryItems(it.value) })
        return ExternalLibraryState(libraries)
    }

    override fun loadState(state: ExternalLibraryState) {
        libraries = state.libraries
//        libraries = state.libraries.mapValues { it.value.items }.toMutableMap()
//        XmlSerializerUtil.copyBean(state, this.state)
//        this.state.loadState(state)
    }

    fun updateLibrary(library: ExternalBibLibrary, bibItems: List<BibtexEntry>) {
        libraries = true
//        libraries[library.name] = bibItems
    }
}