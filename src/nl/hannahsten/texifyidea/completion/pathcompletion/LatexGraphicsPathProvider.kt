package nl.hannahsten.texifyidea.completion.pathcompletion

import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiFile
import nl.hannahsten.texifyidea.psi.LatexNormalText
import nl.hannahsten.texifyidea.util.childrenOfType
import nl.hannahsten.texifyidea.util.files.commandsInFileSet
import nl.hannahsten.texifyidea.util.files.findRootFile
import java.io.File

/**
 * @author Lukas Heiligenbrunner
 */
class LatexGraphicsPathProvider : LatexPathProviderBase() {
    override fun selectScanRoots(file: PsiFile): ArrayList<VirtualFile> {
        val paths = getProjectRoots()

        // Add last included graphicspaths.
        val graphicsPath = file.commandsInFileSet().filter { it.commandToken.text == "\\graphicspath" }
        if (graphicsPath.isNotEmpty()) {
            graphicsPath.last().parameterList.first { firstParam -> firstParam.requiredParam != null }
                    .childrenOfType(LatexNormalText::class).forEach { path ->
                        // Check if graphicspath is an absolute or relative path
                        if (File(path.text).isAbsolute) {
                            file.findRootFile().containingDirectory.virtualFile.fileSystem.findFileByPath(path.text)?.apply {
                                paths.add(this)
                            }
                        }
                        else {
                            file.originalFile.findRootFile().containingDirectory.virtualFile.findFileByRelativePath(path.text)?.apply {
                                paths.add(this)
                            }
                        }
                    }
        }

        return paths
    }

    override fun searchFolders(): Boolean = true

    override fun searchFiles(): Boolean = true
}