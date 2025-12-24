package nl.hannahsten.texifyidea.intentions

import com.intellij.openapi.command.CommandProcessor
import com.intellij.openapi.command.UndoConfirmationPolicy
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiFile
import nl.hannahsten.texifyidea.inspections.latex.codestyle.LatexTooLargeSectionInspection.InspectionFix.Companion.findLabel
import nl.hannahsten.texifyidea.inspections.latex.codestyle.LatexTooLargeSectionInspection.Util
import nl.hannahsten.texifyidea.inspections.latex.codestyle.LatexTooLargeSectionInspection.Util.findNextSection
import nl.hannahsten.texifyidea.psi.LatexCommands
import nl.hannahsten.texifyidea.ui.CreateFileDialog
import nl.hannahsten.texifyidea.util.files.document
import nl.hannahsten.texifyidea.util.files.findRootFile
import nl.hannahsten.texifyidea.util.files.isLatexFile
import nl.hannahsten.texifyidea.util.files.writeToFileUndoable
import nl.hannahsten.texifyidea.util.formatAsFileName
import nl.hannahsten.texifyidea.util.isTestProject
import nl.hannahsten.texifyidea.util.parser.findIndentation
import nl.hannahsten.texifyidea.util.parser.parentOfType
import nl.hannahsten.texifyidea.util.removeAll
import nl.hannahsten.texifyidea.util.runWriteAction
import java.io.File

/**
 * @author Hannah Schellekens
 */
open class LatexMoveSectionToFileIntention : TexifyIntentionBase("Move section contents to separate file") {

    private val affectedCommands = setOf("\\section", "\\chapter")

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
        val (start, end, text) = Util.findTextUntilNextSection(label, sectionCommand, document, nextCmd)

        // Create new file.
        val fileNameBraces = sectionCommand.requiredParameterText(0) ?: return
        // Remove the braces of the LaTeX command before creating a filename of it.
        val fileName = fileNameBraces.removeAll("{", "}")
            .formatAsFileName()
        val root = file.findRootFile().containingDirectory?.virtualFile?.canonicalPath ?: return

        // Display a dialog to ask for the location and name of the new file.
        val filePath = if (isTestProject().not()) {
            CreateFileDialog(file.containingDirectory?.virtualFile?.canonicalPath, fileName.formatAsFileName())
                .newFileFullPath ?: return
        }
        else file.containingDirectory?.virtualFile?.canonicalPath + File.separator + fileName

        // Execute write actions.
        runWriteAction {
            CommandProcessor.getInstance().executeCommand(project, {
                val createdFile = writeToFileUndoable(project, filePath, text, root)

                document.deleteString(start, end)
                val indent = sectionCommand.findIndentation()
                document.insertString(start, "\n$indent\\input{${createdFile.dropLast(4)}}\n\n")
            }, "Move Section to File", "Texify", UndoConfirmationPolicy.DO_NOT_REQUEST_CONFIRMATION)
        }
    }
}