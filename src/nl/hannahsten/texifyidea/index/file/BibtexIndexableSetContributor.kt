package nl.hannahsten.texifyidea.index.file

import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.openapi.vfs.VirtualFileManager
import com.intellij.util.indexing.IndexableSetContributor
import nl.hannahsten.texifyidea.util.files.psiFile
import kotlin.io.path.Path

class BibtexIndexableSetContributor : IndexableSetContributor() {
    override fun getAdditionalRootsToIndex(): MutableSet<VirtualFile> {
//        TODO("Get file paths from library tool")
        val file = VirtualFileManager.getInstance().findFileByNioPath(Path("/home/abby/Documents/test/bibtex/test.bib"))
            ?: return emptySet<VirtualFile>().toMutableSet()
        return mutableSetOf(file)
    }

    /**
     * Only index bib files that are not in the project. The bib files in the project are already indexed by the regular indices.
     */
    override fun acceptFile(file: VirtualFile, root: VirtualFile, project: Project?): Boolean {
        return project?.let { file.psiFile(project) } == null
    }
}