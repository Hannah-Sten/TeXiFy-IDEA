package nl.hannahsten.texifyidea.completion

import com.intellij.openapi.vfs.VirtualFile

/**
 * @author Lukas Heiligenbrunner
 */
class LatexFolderProvider : LatexPathProviderBase() {
    override fun selectScanRoots(): ArrayList<VirtualFile> {
        return getProjectRoots()
    }

    override fun searchFolders(): Boolean = true

    override fun searchFiles(): Boolean = false
}