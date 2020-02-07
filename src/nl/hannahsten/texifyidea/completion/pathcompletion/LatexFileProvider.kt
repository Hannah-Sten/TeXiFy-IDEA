package nl.hannahsten.texifyidea.completion.pathcompletion

import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiFile
import nl.hannahsten.texifyidea.completion.pathcompletion.LatexPathProviderBase
import kotlin.collections.ArrayList

/**
 * @author Hannah Schellekens
 */
class LatexFileProvider : LatexPathProviderBase() {
    override fun selectScanRoots(file: PsiFile): ArrayList<VirtualFile> {
        return getProjectRoots()
    }

    override fun searchFolders(): Boolean = true

    override fun searchFiles(): Boolean = true

}
