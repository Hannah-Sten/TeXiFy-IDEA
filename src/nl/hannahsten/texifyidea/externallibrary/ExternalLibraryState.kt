package nl.hannahsten.texifyidea.externallibrary

import com.intellij.util.xmlb.annotations.OptionTag
import nl.hannahsten.texifyidea.psi.BibtexEntry

data class ExternalLibraryState(
    @OptionTag(converter = LibraryStateConverter::class)
    var libraries: Map<String, List<BibtexEntry>> = emptyMap()
)