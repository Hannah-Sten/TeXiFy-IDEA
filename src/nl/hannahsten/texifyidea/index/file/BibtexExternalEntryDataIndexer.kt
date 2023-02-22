package nl.hannahsten.texifyidea.index.file

import com.intellij.util.indexing.DataIndexer
import com.intellij.util.indexing.FileContent
import nl.hannahsten.texifyidea.psi.BibtexEntry
import nl.hannahsten.texifyidea.remotelibraries.state.BibtexEntryListConverter

object BibtexExternalEntryDataIndexer : DataIndexer<String, BibtexEntry, FileContent> {
    override fun map(inputData: FileContent): MutableMap<String, BibtexEntry> {
        return inputData.contentAsText
            // Split at identifier so we parse one item at a time to avoid that if one item doesn't get through our
            // parser we do not miss out on all the correct entries as well.
            .splitToSequence("@[^,{}\\(\\)\\\"#%'=~\\\\ \\n]+\\\\{")
            .map { BibtexEntryListConverter.fromString(it).firstOrNull() }
            .filterNotNull()
            .associateBy { it.identifier }
            .toMutableMap()
    }
}