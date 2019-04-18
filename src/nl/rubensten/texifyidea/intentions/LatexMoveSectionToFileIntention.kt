package nl.rubensten.texifyidea.intentions

import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiFile
import nl.rubensten.texifyidea.inspections.latex.LatexTooLargeSectionInspection.Companion.findNextSection
import nl.rubensten.texifyidea.inspections.latex.LatexTooLargeSectionInspection.InspectionFix.Companion.findLabel
import nl.rubensten.texifyidea.psi.LatexCommands
import nl.rubensten.texifyidea.ui.CreateFileDialog
import nl.rubensten.texifyidea.util.*

/**
 * @author Ruben Schellekens
 */
open class LatexMoveSectionToFileIntention : TexifyIntentionBase("Move section contents to seperate file") {

    companion object {

        private val affectedCommands = setOf("\\section", "\\chapter")
    }

    // Focusing new dialogs when in write action throws an exception.
    override fun startInWriteAction() = false

    override fun isAvailable(project: Project, editor: Editor?, file: PsiFile?): Boolean {
        if (editor == null || file == null || !file.isLatexFile()) {
            return false
        }

        val element = file.findElementAt(editor.caretModel.offset) ?: return false
        val selected = element as? LatexCommands ?: element.parentOfType(LatexCommands::class) ?: return false
        return selected.name in affectedCommands
    }

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
        val end = (nextCmd?.textOffset ?: document.textLength ?: return) - cmdIndent.length
        val text = document.getText(TextRange(start, end)).trimEnd().removeIndents()

        // Create new file.
        val fileNameBraces = sectionCommand.requiredParameter(0) ?: return
        // Remove the braces of the LaTeX command before creating a filename of it
        val fileName = fileNameBraces.removeAll("{", "}")
                .formatAsFileName()
        val root = file.findRootFile().containingDirectory.virtualFile.canonicalPath ?: return

        val filePath = CreateFileDialog(file.containingDirectory.virtualFile.canonicalPath!!, fileName)
                .newFileFullPath ?: return

        // Execute write actions.
        runWriteAction {
            val createdFile = createFile(filePath, text)
            document.deleteString(start, end)
            val pathRelativeToRoot = createdFile.absolutePath.replace("$root/", "")

            val createdFileName = pathRelativeToRoot
                    .substring(0, pathRelativeToRoot.length - 4)
                    .replace(" ", "-")
                    .decapitalize()
            val indent = sectionCommand.findIndentation()
            document.insertString(start, "\n$indent\\input{$createdFileName}\n\n")
        }
    }
}