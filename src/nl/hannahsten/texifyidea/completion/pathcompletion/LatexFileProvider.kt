package nl.hannahsten.texifyidea.completion.pathcompletion

import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiFile
import nl.hannahsten.texifyidea.index.LatexIncludesIndex
import nl.hannahsten.texifyidea.util.files.getParentDirectoryByImportPaths

/**
 * @author Hannah Schellekens
 */
class LatexFileProvider : LatexPathProviderBase() {

    override fun selectScanRoots(file: PsiFile): List<VirtualFile> {
        val searchDirs = getProjectRoots().toMutableList()
        val allIncludeCommands = LatexIncludesIndex.Util.getItems(file)
        for (command in allIncludeCommands) {
            searchDirs.addAll(getParentDirectoryByImportPaths(command))
        }
        return searchDirs
    }

    override fun searchFolders(): Boolean = true

    override fun searchFiles(): Boolean = true
}
