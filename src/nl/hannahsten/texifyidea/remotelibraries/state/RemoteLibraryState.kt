package nl.hannahsten.texifyidea.remotelibraries.state

import com.intellij.util.xmlb.annotations.OptionTag
import nl.hannahsten.texifyidea.psi.BibtexEntry

data class RemoteLibraryState(
    @OptionTag(converter = LibraryStateConverter::class)
    var libraries: Map<String, List<BibtexEntry>> = emptyMap()
)