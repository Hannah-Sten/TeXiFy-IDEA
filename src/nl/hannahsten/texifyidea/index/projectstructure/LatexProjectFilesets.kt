package nl.hannahsten.texifyidea.index.projectstructure

import com.intellij.openapi.vfs.VirtualFile

/**
 * Describes the filesets of a project, containing all filesets and a mapping from files to their fileset data.
 */
data class LatexProjectFilesets(
    val filesets: Set<Fileset>,
    val mapping: Map<VirtualFile, FilesetData>,
) {
    fun getData(file: VirtualFile): FilesetData? = mapping[file]

    override fun equals(other: Any?): Boolean {
        // we only need to compare the filesets, as they uniquely determine the mapping
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as LatexProjectFilesets

        return filesets == other.filesets
    }

    override fun hashCode(): Int = filesets.hashCode()
}