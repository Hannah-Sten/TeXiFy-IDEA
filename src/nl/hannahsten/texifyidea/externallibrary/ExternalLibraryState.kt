package nl.hannahsten.texifyidea.externallibrary

import com.intellij.util.xmlb.annotations.OptionTag
import nl.hannahsten.texifyidea.psi.BibtexEntry

data class ExternalLibraryState(
    @OptionTag(converter = BibtexEntryListConverter::class)
    var libraries: List<BibtexEntry> = listOf()
)

//data class LibraryItems(
//    val items: List<BibtexEntry> = emptyList()
//)