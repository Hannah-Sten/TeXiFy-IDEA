package nl.hannahsten.texifyidea.completion.pathcompletion

import com.intellij.codeInsight.completion.CompletionParameters
import com.intellij.openapi.vfs.VirtualFile
import nl.hannahsten.texifyidea.index.NewSpecialCommandsIndex
import nl.hannahsten.texifyidea.util.files.findRelativeSearchPathsForImportCommands

/**
 * Provide base folder for autocompleting folders.
 */
object LatexFolderProvider : LatexContextAwarePathProviderBase() {

    override fun selectScanRoots(parameters: CompletionParameters): List<VirtualFile> {
        val searchDirs = getProjectRoots(parameters).toMutableList()
        NewSpecialCommandsIndex.getAllFileInputsInFileset(parameters.originalFile).forEach { command ->
            searchDirs.addAll(findRelativeSearchPathsForImportCommands(command))
        }
        return searchDirs
    }

    override fun searchFolders(): Boolean = true

    override fun searchFiles(): Boolean = false
}