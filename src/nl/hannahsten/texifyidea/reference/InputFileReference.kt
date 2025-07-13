package nl.hannahsten.texifyidea.reference

import com.intellij.openapi.util.TextRange
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiManager
import com.intellij.psi.PsiReferenceBase
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
    val key: String,
    /**
     * The range of the file name within the command text.
     * This is used to replace the file name when renaming the file.
     */
    val range: TextRange,
    val files: Set<VirtualFile>
) : PsiReferenceBase<LatexCommands>(element) {

    init {
        rangeInElement = range
    }

    companion object {

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
    }

    override fun resolve(): PsiFile? {
        if (!element.isValid) return null
//        val refInfo = LatexProjectStructure.commandFileReferenceInfo(element)
        val project = element.project
//        val virtualFiles = refInfo[range] ?: return null
        val psiManager = PsiManager.getInstance(project)
        return files.firstNotNullOfOrNull { psiManager.findFile(it) }
    }

    override fun handleElementRename(newElementName: String): PsiElement {
        return handleElementRename(element, newElementName, true, key, range)
    }

    // Required for moving referenced files
    override fun bindToElement(givenElement: PsiElement): PsiElement {
        val newFile = givenElement as? PsiFile ?: return this.element
        // Assume LaTeX will accept paths relative to the root file
        val newFileName = newFile.virtualFile?.path?.toRelativePath(this.element.containingFile.findRootFile().virtualFile.parent.path) ?: return this.element
        return handleElementRename(element, newFileName, false, key, range)
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