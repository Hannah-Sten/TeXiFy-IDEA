package nl.hannahsten.texifyidea.index.file

import com.intellij.openapi.vfs.VirtualFile
import com.intellij.openapi.vfs.VirtualFileManager
import com.intellij.util.indexing.IndexableSetContributor
import kotlin.io.path.Path

class BibtexIndexableSetContributor : IndexableSetContributor() {
    override fun getAdditionalRootsToIndex(): MutableSet<VirtualFile> {
//        TODO("Get file paths from library tool")
        val file = VirtualFileManager.getInstance().findFileByNioPath(Path("/home/abby/Documents/test/bibtex/test.bib"))
            ?: return emptySet<VirtualFile>().toMutableSet()
        return mutableSetOf(file)
    }
}