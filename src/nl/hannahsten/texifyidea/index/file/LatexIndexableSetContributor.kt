package nl.hannahsten.texifyidea.index.file

import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.util.indexing.IndexableSetContributor

class LatexIndexableSetContributor : IndexableSetContributor() {
    override fun getAdditionalRootsToIndex(): MutableSet<VirtualFile> {
        // todo
        val file = LocalFileSystem.getInstance().findFileByPath("/home/thomas/texlive/2020/texmf-dist/source/latex/algorithms")
        return mutableSetOf(file).filterNotNull().toMutableSet()
    }
}