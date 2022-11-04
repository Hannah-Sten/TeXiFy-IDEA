package nl.hannahsten.texifyidea.intentions

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.command.CommandProcessor
import com.intellij.openapi.command.UndoConfirmationPolicy
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.openapi.roots.ProjectRootManager
import com.intellij.openapi.util.TextRange
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiFileFactory
import com.intellij.psi.PsiManager
import nl.hannahsten.texifyidea.file.LatexFile
import nl.hannahsten.texifyidea.file.LatexFileType
import nl.hannahsten.texifyidea.inspections.latex.codestyle.LatexTooLargeSectionInspection.Companion.findNextSection
import nl.hannahsten.texifyidea.inspections.latex.codestyle.LatexTooLargeSectionInspection.InspectionFix.Companion.findLabel
import nl.hannahsten.texifyidea.psi.LatexCommands
import nl.hannahsten.texifyidea.ui.CreateFileDialog
import nl.hannahsten.texifyidea.util.*
import nl.hannahsten.texifyidea.util.files.document
import nl.hannahsten.texifyidea.util.files.findRootFile
import nl.hannahsten.texifyidea.util.files.getUniqueFileName
import nl.hannahsten.texifyidea.util.files.isLatexFile
import java.io.File
import java.io.IOException
import java.nio.file.Path
import kotlin.io.path.pathString

/**
 * @author Hannah Schellekens
 */
open class LatexMoveSectionToFileIntention : TexifyIntentionBase("Move section contents to separate file") {

    companion object {

        private val affectedCommands = setOf("\\section", "\\chapter")
    }

    override fun isAvailable(project: Project, editor: Editor?, file: PsiFile?): Boolean {
        if (editor == null || file == null || !file.isLatexFile()) {
            return false
        }

        val element = file.findElementAt(editor.caretModel.offset) ?: return false
        val selected = element as? LatexCommands ?: element.parentOfType(LatexCommands::class) ?: return false
        return selected.name in affectedCommands
    }

    // Focusing new dialogs when in write action throws an exception.
    override fun startInWriteAction() = false

    override fun invoke(project: Project, editor: Editor?, file: PsiFile?) {
        if (editor == null || file == null) {
            return
        }

        // Find related elements.
        val element = file.findElementAt(editor.caretModel.offset) ?: return
        val sectionCommand = element as? LatexCommands ?: element.parentOfType(LatexCommands::class) ?: return
        val document = file.document() ?: return
        val label = findLabel(sectionCommand)
        val nextCmd = findNextSection(sectionCommand)

        // Find text.
        val start = label?.endOffset() ?: sectionCommand.endOffset()
        val cmdIndent = document.lineIndentation(document.getLineNumber(nextCmd?.textOffset ?: 0))
        val end = (nextCmd?.textOffset ?: document.textLength) - cmdIndent.length
        val text = document.getText(TextRange(start, end)).trimEnd().removeIndents()

        // Create new file.
        val fileNameBraces = sectionCommand.requiredParameter(0) ?: return
        // Remove the braces of the LaTeX command before creating a filename of it.
        val fileName = fileNameBraces.removeAll("{", "}")
            .formatAsFileName()
        val root = file.findRootFile().containingDirectory?.virtualFile?.canonicalPath ?: return

        // Display a dialog to ask for the location and name of the new file.
        val filePath = if (project.isTestProject().not()) {
            CreateFileDialog(file.containingDirectory?.virtualFile?.canonicalPath, fileName.formatAsFileName())
                .newFileFullPath ?: return
        }
        else file.containingDirectory?.virtualFile?.canonicalPath + File.separator + fileName

        // Execute write actions.
        ApplicationManager.getApplication().runWriteAction {
            CommandProcessor.getInstance().executeCommand(project, {
                // Create file...but not on fs yet
                val fileFactory = PsiFileFactory.getInstance(project)
                val newfile = fileFactory.createFileFromText(
                    getUniqueFileName(
                        Path.of(filePath).fileName.toString().appendExtension("tex"),
                        Path.of(filePath).parent.pathString
                    ),
                    LatexFileType,
                    text
                )

                val projectRootManager = ProjectRootManager.getInstance(project)
                val allRoots = projectRootManager.contentRoots + projectRootManager.contentSourceRoots

                // The following is going to resolve the PsiDirectory that we need to add the new file to.
                var relativePath = ""
                var bestRoot: VirtualFile? = null
                for (testFile in allRoots) {
                    val rootPath = testFile.path
                    if (root.startsWith(rootPath)) {
                        relativePath = root.substring(rootPath.length)
                        bestRoot = testFile
                        break
                    }
                }
                if (bestRoot == null) {
                    throw IOException("Can't find '$root' among roots")
                }

                val dirs = relativePath.split("/".toRegex()).dropLastWhile { it.isEmpty() }
                    .toTypedArray()

                var resultDir: VirtualFile = bestRoot
                if (dirs.isNotEmpty()) {
                    var i = 0
                    if (dirs[0].isEmpty()) i = 1

                    while (i < dirs.size) {
                        val subdir = resultDir.findChild(dirs[i])
                        if (subdir != null) {
                            if (!subdir.isDirectory) {
                                throw IOException("Expected resultDir, but got non-resultDir: " + subdir.path)
                            }
                        }
                        if (subdir != null) {
                            resultDir = subdir
                        }
                        else
                            throw Exception("Could not locate directory")
                        i += 1
                    }
                }

                // Actually create the file on fs
                val thing = PsiManager.getInstance(project).findDirectory(resultDir)?.add(newfile)

                // back to your regularly scheduled programming
                val fileNameRelativeToRoot = (thing as LatexFile).virtualFile.path
                    .replace(File.separator, "/")
                    .replace("$root/", "")

                document.deleteString(start, end)
                val indent = sectionCommand.findIndentation()
                document.insertString(start, "\n$indent\\input{${fileNameRelativeToRoot.dropLast(4)}}\n\n")
            }, "Move Section to File", "Texify", UndoConfirmationPolicy.DO_NOT_REQUEST_CONFIRMATION)
        }
    }
}