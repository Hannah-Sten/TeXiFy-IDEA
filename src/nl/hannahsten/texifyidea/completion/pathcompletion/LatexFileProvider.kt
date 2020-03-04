package nl.hannahsten.texifyidea.completion.pathcompletion

import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiFile

/**
 * @author Hannah Schellekens
 */
class LatexFileProvider : LatexPathProviderBase() {
    override fun selectScanRoots(file: PsiFile): ArrayList<VirtualFile> {
        // todo plus import package
        return getProjectRoots()
    }

    override fun searchFolders(): Boolean = true

    override fun searchFiles(): Boolean = true

}
