package nl.hannahsten.texifyidea.index.file

import com.intellij.openapi.project.Project
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.util.indexing.*
import com.intellij.util.io.DataExternalizer
import com.intellij.util.io.EnumeratorStringDescriptor
import com.intellij.util.io.KeyDescriptor
import nl.hannahsten.texifyidea.file.BibtexFileType
import nl.hannahsten.texifyidea.psi.BibtexEntry

class BibtexExternalEntryIndex : FileBasedIndexExtension<String, BibtexEntry>() {

    companion object {

        val id = ID.create<String, BibtexEntry>("nl.hannahsten.texifyidea.external.bibtex.entries")

        fun getAllValues(project: Project): Sequence<BibtexEntry> = FileBasedIndex.getInstance()
            .getAllKeys(id, project)
            .asSequence()
            .flatMap { FileBasedIndex.getInstance().getValues(id, it, GlobalSearchScope.everythingScope(project)) }
    }

    override fun getName(): ID<String, BibtexEntry> = id

    override fun getIndexer(): DataIndexer<String, BibtexEntry, FileContent> {
        return BibtexExternalEntryDataIndexer
    }

    override fun getKeyDescriptor(): KeyDescriptor<String> {
        return EnumeratorStringDescriptor.INSTANCE
    }

    override fun getValueExternalizer(): DataExternalizer<BibtexEntry> {
        return BibtexEntryExternalizer
    }

    override fun getVersion(): Int = 1

    override fun getInputFilter(): FileBasedIndex.InputFilter {
        return DefaultFileTypeSpecificInputFilter(BibtexFileType)
    }

    override fun dependsOnFileContent(): Boolean = true

    override fun traceKeyHashToVirtualFileMapping(): Boolean = true
}