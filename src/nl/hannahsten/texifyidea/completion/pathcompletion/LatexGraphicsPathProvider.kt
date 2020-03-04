package nl.hannahsten.texifyidea.completion.pathcompletion

import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiFile
import nl.hannahsten.texifyidea.util.files.findRootFile
import nl.hannahsten.texifyidea.util.files.findVirtualFileByAbsoluteOrRelativePath
import nl.hannahsten.texifyidea.util.files.getGraphicsPaths

/**
 * @author Lukas Heiligenbrunner
 */
class LatexGraphicsPathProvider : LatexPathProviderBase() {
    override fun selectScanRoots(file: PsiFile): ArrayList<VirtualFile> {
        val paths = getProjectRoots()

        val rootDirectory = file.findRootFile().containingDirectory.virtualFile
        getGraphicsPaths(file.project).forEach {
            paths.add(rootDirectory.findVirtualFileByAbsoluteOrRelativePath(it) ?: return@forEach)
        }

        return paths
    }

    override fun searchFolders(): Boolean = true

    override fun searchFiles(): Boolean = true
}