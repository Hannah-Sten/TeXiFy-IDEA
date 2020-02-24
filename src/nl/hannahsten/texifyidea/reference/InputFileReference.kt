package nl.hannahsten.texifyidea.reference

import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiManager
import com.intellij.psi.PsiReferenceBase
import nl.hannahsten.texifyidea.psi.LatexCommands
import nl.hannahsten.texifyidea.psi.LatexPsiHelper
import nl.hannahsten.texifyidea.util.Magic
import nl.hannahsten.texifyidea.util.files.findFile
import nl.hannahsten.texifyidea.util.files.findRootFile
import nl.hannahsten.texifyidea.util.files.getExternalFile
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader

/**
 * @author Abby Berkers
 */
class InputFileReference(element: LatexCommands, val range: TextRange) : PsiReferenceBase<LatexCommands>(element) {
    init {
        rangeInElement = range
    }

    val key by lazy {
        rangeInElement.substring(element.text)
    }

    override fun resolve(): PsiElement? {
        // Find the sources root of the current file.
        val root = element.containingFile.findRootFile()
                .containingDirectory.virtualFile
        // Find the target file, by first searching through the project directory.
        val targetFile = root.findFile(key, Magic.File.includeExtensions)
                // When the file does not exist in the project directory, look for
                // it elsewhere using the kpsewhich command.
                ?: element.getFileNameWithExtensions(key)?.map { runKpsewhich(it) }?.map {
                    getExternalFile(it ?: return null)
                }?.firstOrNull { it != null }
                // If kpsewhich can also not find it, return null.
                ?: return null
        // Return a reference to the target file.
        return PsiManager.getInstance(element.project).findFile(targetFile)
    }

    override fun handleElementRename(newElementName: String): PsiElement {
        // A file has been renamed and we are given a new filename, to be replaced in the parameter text of the current command
        // It seems to be problematic to find the old filename we want to replace
        val commandText = "${myElement?.name}{$newElementName}"
        val oldNode = myElement?.node
        val newNode = LatexPsiHelper(element.project).createFromText(commandText).firstChild.node
        if (oldNode == null) {
            myElement?.parent?.node?.addChild(newNode)
        }
        else {
            myElement.parent.node.replaceChild(oldNode, newNode)
        }
        return myElement
    }

    /**
     * Create a set possible complete file names (including extension), based on
     * the command that includes a file, and the name of the file.
     */
    private fun LatexCommands.getFileNameWithExtensions(fileName: String): HashSet<String>? {
        val extension: HashSet<String>? = Magic.Command.includeOnlyExtensions[this.commandToken.text]
        return extension?.map { "$fileName.$it" }?.toHashSet()
    }

    companion object {
        private fun runKpsewhich(arg: String): String? = try {
            BufferedReader(InputStreamReader(Runtime.getRuntime().exec(
                    "kpsewhich $arg"
            ).inputStream)).readLine()  // Returns null if no line read.
        } catch (e: IOException) {
            null
        }
    }
}