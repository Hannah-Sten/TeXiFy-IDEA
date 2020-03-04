package nl.hannahsten.texifyidea.completion.pathcompletion

import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiFile

/**
 * @author Lukas Heiligenbrunner
 */
class LatexFolderProvider : LatexPathProviderBase() {
    override fun selectScanRoots(file: PsiFile): ArrayList<VirtualFile> {
        // todo import package (subimport esp.)
        return getProjectRoots()
    }

    override fun searchFolders(): Boolean = true

    override fun searchFiles(): Boolean = false
}