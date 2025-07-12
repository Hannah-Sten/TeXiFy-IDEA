package nl.hannahsten.texifyidea.reference

import com.intellij.openapi.util.TextRange
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiManager
import com.intellij.psi.PsiReferenceBase
import nl.hannahsten.texifyidea.algorithm.BFS
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
    val range: TextRange,
    val extensions: Collection<String>,
    val supportsAnyExtension: Boolean,
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

    val key by lazy {
        rangeInElement.substring(element.text)
    }

    override fun resolve(): PsiFile? {
        return resolve(true)
    }

    /**
     * @param lookForInstalledPackages
     *              Whether to look for packages installed elsewhere on the filesystem.
     *              Set to false when it would make the operation too expensive, for example when trying to
     *              calculate the fileset of many files.
     * @param givenRootFile Used to avoid unnecessarily recalculating the root file.
     * @param isBuildingFileset
     *              True if we are building the fileset.
     *              If false we also need to resolve to graphics files. Doing so is really expensive at
     *              the moment (at least until the implementation in LatexGraphicsPathProvider is improved):
     *              for projects with 500 include commands in hundreds of files this can take 10 seconds in total if
     *              you call this function for every include command.
     *              However, note that doing only one resolve is not too expensive at all
     *              (10 seconds divided by 500 commands/resolves) so this is not a problem when doing only one resolve
     *              (if requested by the user).
     */
    fun resolve(lookForInstalledPackages: Boolean, givenRootFile: VirtualFile? = null, isBuildingFileset: Boolean = false, checkImportPath: Boolean = true, checkAddToLuatexPath: Boolean = true): PsiFile? {
        // IMPORTANT In this method, do not use any functionality which makes use of the file set,
        // because this function is used to find the file set so that would cause an infinite loop

        if (!element.isValid) return null
        val files = LatexProjectStructure.commandReferringFiles(element)
        if (files.isEmpty()) {
            // If the command does not refer to any files, we cannot resolve it
            return null
        }
        // Return a reference to the target file.
        return PsiManager.getInstance(element.project).findFile(files.first())
    }

    /**
     * Try to find the file anywhere in the project. Returns the first match.
     * Might be expensive for large projects because of recursively visiting all directories, not sure.
     */
    fun findAnywhereInProject(fileName: String): VirtualFile? {
        val basePath = if (element.project.isTestProject().not()) {
            LocalFileSystem.getInstance().findFileByPath(element.project.basePath ?: return null) ?: return null
        }
        else {
            element.containingFile.virtualFile.parent ?: return null
        }
        BFS(basePath, { file -> file.children.toList() }).apply {
            iterationAction = { file: VirtualFile ->
                if (file.nameWithoutExtension == fileName && file.extension in extensions) {
                    BFS.BFSAction.ABORT
                }
                else {
                    BFS.BFSAction.CONTINUE
                }
            }
            execute()
            return end
        }
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