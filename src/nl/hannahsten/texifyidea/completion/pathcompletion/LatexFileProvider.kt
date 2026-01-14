package nl.hannahsten.texifyidea.completion.pathcompletion

import com.intellij.codeInsight.completion.CompletionParameters
import com.intellij.openapi.vfs.VirtualFile
import nl.hannahsten.texifyidea.index.NewSpecialCommandsIndex
import nl.hannahsten.texifyidea.util.files.getParentDirectoryByImportPaths

/**
 * @author Hannah Schellekens
 */
object LatexFileProvider : LatexContextAwarePathProviderBase() {

    override fun selectScanRoots(parameters: CompletionParameters): List<VirtualFile> {
        val searchDirs = getProjectRoots(parameters).toMutableList()
        val allIncludeCommands = NewSpecialCommandsIndex.getAllFileInputsInFileset(parameters.originalFile)
        for (command in allIncludeCommands) {
            searchDirs.addAll(getParentDirectoryByImportPaths(command))
        }
        return searchDirs
    }

    override fun searchFolders(): Boolean = true

    override fun searchFiles(): Boolean = true
}
