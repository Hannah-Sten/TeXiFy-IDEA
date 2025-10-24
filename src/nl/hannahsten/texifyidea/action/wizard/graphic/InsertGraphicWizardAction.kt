package nl.hannahsten.texifyidea.action.wizard.graphic

import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.PlatformDataKeys
import com.intellij.openapi.application.runInEdt
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.fileEditor.TextEditor
import com.intellij.openapi.project.Project
import com.intellij.openapi.roots.ProjectRootManager
import com.intellij.openapi.util.io.toNioPathOrNull
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.openapi.vfs.toNioPathOrNull
import com.intellij.psi.PsiFile
import nl.hannahsten.texifyidea.lang.LatexPackage
import nl.hannahsten.texifyidea.lang.graphic.CaptionLocation
import nl.hannahsten.texifyidea.util.*
import nl.hannahsten.texifyidea.util.files.*
import java.io.File
import kotlin.io.path.invariantSeparatorsPathString

/**
 * Action that shows a dialog with a graphic insertion wizard, and inserts the graphic as latex at the location of the
 * cursor.
 *
 * @author Hannah Schellekens
 */
class InsertGraphicWizardAction(private val initialFile: File? = null) : AnAction() {

    /**
     * Opens and handles the graphic insertion wizard.
     */
    fun executeAction(file: VirtualFile, project: Project, editor: TextEditor? = null, caretPosition: Int? = null) {
        val editor = project.selectedTextEditorOrWarning() ?: editor ?: return
        runInEdt {
            val text = showDialogAndGetText(editor, file, project) ?: return@runInEdt
            if (caretPosition != null) {
                editor.editor.insertAndMove(caretPosition, text)
            }
            else {
                editor.editor.insertAtCaretAndMove(text)
            }
        }
    }

    /**
     * Show user dialog and get the text to insert.
     */
    fun showDialogAndGetText(
        editor: TextEditor,
        file: VirtualFile,
        project: Project
    ): String? {
        val document = editor.editor.document

        // Get the indentation from the current line.
        val indent = document.lineIndentationByOffset(editor.editor.caretOffset())

        // Create the dialog.
        val dialogWrapper = InsertGraphicWizardDialogWrapper(initialFilePath = initialFile?.absolutePath ?: "")

        // If the user pressed OK, do stuff.
        if (!dialogWrapper.showAndGet()) return null

        // Handle result.
        val graphicData = dialogWrapper.extractData()
        val psiFile = file.psiFile(project)
        psiFile?.let { graphicData.importPackages(it) }
        val rootFile = psiFile?.findRootFile()
        return buildGraphicString(project, graphicData, indent, rootFile)
    }

    override fun actionPerformed(e: AnActionEvent) {
        val file = e.getData(PlatformDataKeys.VIRTUAL_FILE) ?: return
        val project = e.getData(PlatformDataKeys.PROJECT) ?: return
        executeAction(file, project)
    }

    override fun update(e: AnActionEvent) {
        super.update(e)

        val file = e.getData(PlatformDataKeys.PSI_FILE)
        val shouldDisplayMenu = file?.isLatexFile() == true
        e.presentation.isVisible = shouldDisplayMenu
    }

    override fun getActionUpdateThread() = ActionUpdateThread.BGT

    private fun buildGraphicString(
        project: Project,
        data: InsertGraphicData,
        indent: String,
        rootFile: PsiFile?,
        tab: String = "    ",
    ): String {
        // Only the graphics (non-centered).
        val toInsert = if (data.center.not() && data.placeInFigure.not()) {
            data.includeCommand(project, rootFile)
        }
        // Centered graphics, but not in a figure.
        else if (data.center && data.placeInFigure.not()) {
            buildString {
                append("\\begin{center}\n")
                append(indent).append(tab).append(data.includeCommand(project, rootFile)).newline()
                append(indent).append("\\end{center}")
            }
        }
        // Insert figure.
        else data.figure(project, indent, tab, rootFile)

        return toInsert
    }

    private fun InsertGraphicData.figure(project: Project, indent: String, tab: String, rootFile: PsiFile?) = buildString {
        append("\\begin{figure}")
        if (positions?.isNotEmpty() == true) {
            append(positionOptions())
        }
        newline()

        if (center) {
            append(indent).append(tab).append("\\centering").newline()
        }

        if (captionLocation == CaptionLocation.ABOVE_GRAPHIC) {
            addCaptionAndLabel(this@figure, indent, tab)
        }

        append(indent).append(tab).append(includeCommand(project, rootFile)).newline()

        if (captionLocation == CaptionLocation.BELOW_GRAPHIC) {
            addCaptionAndLabel(this@figure, indent, tab)
        }

        append(indent).append("\\end{figure}")
    }

    private fun StringBuilder.addCaptionAndLabel(data: InsertGraphicData, indent: String, tab: String) {
        append(indent).append(tab).append(data.captionCommand()).newline()
        append(indent).append(tab).append("\\label{").append(data.label ?: "").append("}").newline()
    }

    private fun InsertGraphicData.includeCommand(project: Project, rootFile: PsiFile?) = buildString {
        append("\\includegraphics")
        if (options.isNotBlank()) {
            append("[").append(options).append("]")
        }
        append("{").append(convertFilePath(project, filePath, rootFile)).append("}")
    }

    private fun InsertGraphicData.convertFilePath(project: Project, absoluteFilePath: String, rootFile: PsiFile?): String {
        val absPath = absoluteFilePath.toNioPathOrNull() ?: return absoluteFilePath
        val rootManager = ProjectRootManager.getInstance(project)

        val filePath = if (relativePath) {
            // We need to match the path given by the user (from e.g. the file chooser dialog) to the root file
            val imageFile = LocalFileSystem.getInstance().findFileByNioFile(absPath)
            if (rootFile != null && imageFile != null) {
                FileUtil.pathRelativeTo(rootFile.virtualFile.toNioPathOrNull()?.parent, imageFile.toNioPathOrNull())?.invariantSeparatorsPathString
            }
            else {
                rootManager.relativizePath(absoluteFilePath)
            } ?: absoluteFilePath
        }
        else absoluteFilePath

        // LaTeX cannot handle backslashes in path, always replace them
        return filePath.removeFileExtension().replace("\\", "/")
    }

    private fun InsertGraphicData.captionCommand() = buildString {
        append("\\caption")
        if (shortCaption?.isNotBlank() == true) {
            append("[").append(shortCaption).append("]")
        }
        append("{").append(caption ?: "").append("}")
    }

    private fun InsertGraphicData.importPackages(file: PsiFile) {
        WriteCommandAction.runWriteCommandAction(file.project) {
            file.insertUsepackage(LatexPackage.GRAPHICX)
            positions?.forEach { location ->
                location.requiredPackage?.let {
                    file.insertUsepackage(it)
                }
            }
        }
    }

    private fun InsertGraphicData.positionOptions() = buildString {
        append("[")
        append(positions?.joinToString("") { it.symbol })
        append("]")
    }
}
