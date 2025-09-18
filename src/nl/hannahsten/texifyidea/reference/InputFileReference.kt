package nl.hannahsten.texifyidea.reference

import com.intellij.openapi.project.Project
import com.intellij.openapi.util.TextRange
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiManager
import com.intellij.psi.PsiReferenceBase
import nl.hannahsten.texifyidea.index.LatexProjectStructure
import nl.hannahsten.texifyidea.psi.LatexCommands
import nl.hannahsten.texifyidea.psi.LatexPsiHelper
import nl.hannahsten.texifyidea.util.*
import nl.hannahsten.texifyidea.util.files.*
import nl.hannahsten.texifyidea.util.magic.CommandMagic

/**
 * Reference to a file, based on the command and the range of the filename within the command text.
 */
class InputFileReference(
    element: LatexCommands,
    /**
     * The text of the file name as it appears in the command without any processing.
     */
    val refText: String,
    /**
     * The range of the file name within the command text.
     * This is used to replace the file name when renaming the file.
     */
    val range: TextRange,
    /**
     * The known set of files that this command refers to, which may be invalid.
     */
    val files: Set<VirtualFile>
) : PsiReferenceBase<LatexCommands>(element) {

    init {
        rangeInElement = range
    }

    companion object {

        fun findValidPSIFiles(files: Iterable<VirtualFile>, project: Project): List<PsiFile> {
            val psiManager = PsiManager.getInstance(project)
            return files.mapNotNull { file ->
                file.takeIf { it.isValid }?.let { psiManager.findFile(it) }
            }
        }

        /**
         * Handle element rename, but taking into account whether the given
         * newElementName is just a filename which we have to replace,
         * or a full relative path (in which case we replace the whole path).
         */
        fun handleElementRename(command: LatexCommands, newElementName: String, elementNameIsJustFilename: Boolean, key: String, range: TextRange): PsiElement {
            // A file has been renamed and we are given a new filename, to be replaced in the parameter text of the current command
            val oldNode = command.node

            val newName = if ((oldNode?.psi as? LatexCommands)?.name in CommandMagic.illegalExtensions.keys) {
                newElementName.removeFileExtension()
            }
            else {
                newElementName
            }

            // Recall that \ is a file separator on Windows
            val newKey = if (elementNameIsJustFilename) key.replaceAfterLast('/', newName, newName) else newName

            // The file may be in a required or optional parameter
            val newText = command.text.replaceRange(range.toIntRange(), newKey)

            val newNode = LatexPsiHelper(command.project).createFromText(newText).firstChild.node ?: return command
            if (oldNode == null) {
                command.parent?.node?.addChild(newNode)
            }
            else {
                command.parent.node.replaceChild(oldNode, newNode)
            }
            return command
        }

        /**
         * Use a text-based search to find the text ranges of the command parameters that refer to files.
         */
        private fun recoverTextRanges(command: LatexCommands, refData: Pair<List<String>, List<Set<VirtualFile>>>?): List<Triple<String, TextRange, Set<VirtualFile>>> {
            refData ?: return emptyList()
            val commandText = command.text
            var offset = 0
            return refData.first.zip(refData.second).mapNotNull { (refText, files) ->
                val shift = commandText.indexOf(refText, offset)
                if (shift < 0) return@mapNotNull null
                // If the text is not found, it means the command was modified and the reference is no longer valid, but we still want to return some files
                offset = shift
                Triple(refText, TextRange.from(offset, refText.length), files)
            }
        }

        /**
         * Get files included by this command built from the fileset information in the project structure.
         */
        fun getIncludedFiles(command: LatexCommands, includePackages: Boolean = true, requireRefresh: Boolean = false): List<PsiFile> {
            val refInfo = LatexProjectStructure.commandFileReferenceInfo(command, requireRefresh) ?: return emptyList()
            if(refInfo.first.isEmpty()) return emptyList()
            return findValidPSIFiles(refInfo.second.flatten(), command.project)
        }

        /**
         * Check if the command includes other files, and if so return [InputFileReference] instances for them.
         * This method is called continuously, so it should be really fast.
         *
         * Use this instead of command.references.filterIsInstance<InputFileReference>(), to avoid resolving references of types that will not be needed.
         */
        fun getFileArgumentsReferences(commands: LatexCommands, requireRefresh: Boolean = false): List<InputFileReference> {
            val rawInfo = LatexProjectStructure.commandFileReferenceInfo(commands, requireRefresh) ?: return emptyList()
            if(rawInfo.first.isEmpty()) return emptyList()
            val refInfoMap = recoverTextRanges(commands, rawInfo)
            return refInfoMap.map { (text, range, files) ->
                InputFileReference(commands, text, range, files)
            }
        }
    }

    override fun resolve(): PsiFile? {
        if (!element.isValid) return null
        val project = element.project
        val psiManager = PsiManager.getInstance(project)
        return files.firstNotNullOfOrNull { f ->
            f.takeIf { it.isValid }?.let { psiManager.findFile(it) }
        }
    }

    override fun handleElementRename(newElementName: String): PsiElement {
        return handleElementRename(element, newElementName, true, refText, range)
    }

    // Required for moving referenced files
    override fun bindToElement(givenElement: PsiElement): PsiElement {
        val newFile = givenElement as? PsiFile ?: return this.element
        // Assume LaTeX will accept paths relative to the root file
        val newFileName = newFile.virtualFile?.path?.toRelativePath(this.element.containingFile.findRootFile().virtualFile.parent.path) ?: return this.element
        return handleElementRename(element, newFileName, false, refText, range)
    }

    /**
     * Create a set possible complete file names (including extension), based on
     * the command that includes a file, and the name of the file.
     */
    private fun LatexCommands.getFileNameWithExtensions(fileName: String): Set<String> {
        val extension = CommandMagic.includeAndExtensions[this.commandToken.text] ?: emptySet()
        return extension.map { "$fileName.$it" }.toSet() + setOf(fileName)
    }
}