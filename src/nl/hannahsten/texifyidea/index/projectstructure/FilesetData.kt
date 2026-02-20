package nl.hannahsten.texifyidea.index.projectstructure

import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.search.GlobalSearchScope

/**
 * Integrated descriptions of all filesets that are related to a specific file.
 */
data class FilesetData(
    val filesets: Set<Fileset>,
    /**
     * The union of all files in the related [filesets].
     */
    val relatedFiles: Set<VirtualFile>,
    /**
     * The scope that contains all files in [relatedFiles].
     */
    val filesetScope: GlobalSearchScope,

    val libraries: Set<String>,

    val externalDocumentInfo: List<ExternalDocumentInfo>
) {
    override fun equals(other: Any?): Boolean {
        // filesets uniquely determine the rest of the data
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as FilesetData

        return filesets == other.filesets
    }

    override fun hashCode(): Int = filesets.hashCode()
}