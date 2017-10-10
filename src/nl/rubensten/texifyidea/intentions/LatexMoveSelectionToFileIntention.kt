package nl.rubensten.texifyidea.intentions

import com.intellij.openapi.application.runWriteAction
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogBuilder
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiFile
import nl.rubensten.texifyidea.TexifyIcons
import nl.rubensten.texifyidea.util.TexifyUtil
import nl.rubensten.texifyidea.util.findRootFile
import nl.rubensten.texifyidea.util.isLatexFile
import nl.rubensten.texifyidea.util.removeIndents
import org.intellij.lang.annotations.Language
import java.awt.event.FocusEvent
import java.awt.event.FocusListener
import javax.swing.*

/**
 * @author Ruben Schellekens
 */
open class LatexMoveSelectionToFileIntention : TexifyIntentionBase("Move selection contents to seperate file") {

    companion object {

        val MINIMUM_SELECTION_LENGTH = 24
    }

    override fun startInWriteAction() = false

    override fun isAvailable(project: Project, editor: Editor?, file: PsiFile?): Boolean {
        if (editor == null || file == null || !file.isLatexFile()) {
            return false
        }

        val selectionSize = selectionOffsets(editor).sumBy { (start, end) -> end - start }
        return selectionSize >= MINIMUM_SELECTION_LENGTH
    }

    override fun invoke(project: Project, editor: Editor?, file: PsiFile?) {
        if (editor == null || file == null) {
            return
        }

        // Ask the user for a new file name.
        val offsets = selectionOffsets(editor)
        val document = editor.document
        val selection = selectionOffsets(editor).first()
        val default = document.getText(TextRange(selection.first, selection.second)).split(Regex("\\s+"))[0]
        val dialogResult = (promptName(default) ?: return) + ".tex"
        if (".tex".equals(dialogResult, true)) {
            return
        }

        // Find text.
        val text = StringBuilder()
        for ((start, end) in offsets) {
            val selected = document.getText(TextRange(start, end)).trimEnd().removeIndents()
            text.append(selected)
        }

        // Manage paths/file names.
        @Language("RegExp")
        val fileName = dialogResult.replace(Regex("(\\.tex)+$", RegexOption.IGNORE_CASE), "")
        val root = file.findRootFile().containingDirectory.virtualFile.canonicalPath

        // Execute write actions.
        runWriteAction {
            val filePath = "$root/$fileName.tex";
            val createdFile = TexifyUtil.createFile(filePath, text.toString())

            for ((start, end) in offsets.reversed()) {
                document.deleteString(start, end)
            }

            val createdFileName = createdFile?.name?.substring(0, createdFile.name.length - 4)
            document.insertString(offsets.first().first, "\\input{$createdFileName}")
        }
    }

    private fun selectionOffsets(editor: Editor): List<Pair<Int, Int>> {
        return editor.caretModel.allCarets
                .map { Pair(it.selectionStart, it.selectionEnd) }
                .toList()
    }

    /**
     * @return `null` when the dialog was cancelled.
     */
    private fun promptName(default: String = ""): String? {
        var result: String? = null
        DialogBuilder().apply {
            setTitle("Move selection to new file")

            // Create components.
            val label = JLabel(
                    """|<html>
                        |<table>
                        |<tr><td>Please enter the filename of the file to be created.<br>(<tt>.tex</tt> optional)</td></tr>
                        |</table>
                        |</html>""".trimMargin(),
                    TexifyIcons.LATEX_FILE_BIG,
                    SwingConstants.LEADING
            )

            val textField = JTextField(default)
            textField.addFocusListener(object : FocusListener {
                override fun focusLost(e: FocusEvent?) {
                }

                override fun focusGained(e: FocusEvent?) {
                    textField.selectAll()
                }
            })

            // Create panel.
            val panel = JPanel()
            panel.layout = BoxLayout(panel, BoxLayout.Y_AXIS)
            panel.add(label)
            panel.add(textField)
            setCenterPanel(panel)

            // Dialog stuff.
            addOkAction()
            setOkOperation {
                dialogWrapper.close(0)
            }

            if (show() == DialogWrapper.OK_EXIT_CODE) {
                result = textField.text
            }
        }

        return result
    }
}