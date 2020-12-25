package nl.hannahsten.texifyidea.index.file

import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.util.indexing.IndexableSetContributor

/**
 * Specify the paths that have to be indexed for the [LatexExternalCommandIndex].
 */
class LatexIndexableSetContributor : IndexableSetContributor() {
    override fun getAdditionalRootsToIndex(): MutableSet<VirtualFile> {
        // todo add all latex sources
        val file = LocalFileSystem.getInstance().findFileByPath("/home/thomas/texlive/2020/texmf-dist/source/latex/siunitx")
        return mutableSetOf(file).filterNotNull().toMutableSet()
    }
}