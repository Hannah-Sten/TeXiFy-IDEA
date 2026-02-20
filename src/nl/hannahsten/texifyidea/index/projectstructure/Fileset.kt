package nl.hannahsten.texifyidea.index.projectstructure

import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.search.GlobalSearchScope
import nl.hannahsten.texifyidea.file.LatexFileType
import nl.hannahsten.texifyidea.util.contentSearchScope
import java.util.*

/**
 * A fileset is a set of files that are related to each other, e.g. a main file and its included files (including itself).
 * One file can belong to multiple filesets, e.g. if it is included in multiple main files.
 *
 * When resolving subfiles, we must use the relative path from the root file rather than the file that contains the input command.
 */
data class Fileset(
    val root: VirtualFile,
    /**
     * The files in the fileset
     */
    val files: SequencedSet<VirtualFile>,
    val libraries: Set<String>,
    val allFileScope: GlobalSearchScope,
    val externalDocumentInfo: List<ExternalDocumentInfo>
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Fileset

        if (root != other.root) return false
        if (files != other.files) return false

        return true
    }

    override fun hashCode(): Int {
        var result = root.hashCode()
        result = 31 * result + files.hashCode()
        return result
    }

    fun texFileScope(project: Project): GlobalSearchScope = allFileScope.restrictedByFileTypes(LatexFileType).intersectWith(project.contentSearchScope)
}