package nl.hannahsten.texifyidea.externallibrary

import com.intellij.openapi.components.*
import com.intellij.util.xmlb.XmlSerializerUtil
import com.intellij.util.xmlb.annotations.OptionTag
import nl.hannahsten.texifyidea.psi.BibtexEntry
import org.xmlpull.v1.XmlSerializer

@State(name = "ExternalBibLibraryManager", storages = [Storage("library.xml")])
class ExternalLibraryManager : PersistentStateComponent<ExternalLibraryManager.ExternalLibraryState> {

    data class ExternalLibraryState(val libraries: MutableMap<String, LibraryItems> = mutableMapOf()) {
        fun loadState(other: ExternalLibraryState) {
            libraries.clear()
            libraries.putAll(other.libraries)
        }

        fun updateLibrary(library: ExternalBibLibrary, bibItems: LibraryItems) {
            libraries[library.name] = bibItems
        }
    }

    data class LibraryItems(
        @OptionTag(converter = BibtexEntryListConverter::class)
        val items: List<BibtexEntry>
    )

    private val state: ExternalLibraryState = ExternalLibraryState()

    override fun getState(): ExternalLibraryState {
        return state
    }

    override fun loadState(state: ExternalLibraryState) {
        XmlSerializerUtil.copyBean(state, this.state)
//        this.state.loadState(state)
    }

    fun updateLibrary(library: ExternalBibLibrary, bibItems: List<BibtexEntry>) {
        this.state.updateLibrary(library, LibraryItems(bibItems))
    }
}