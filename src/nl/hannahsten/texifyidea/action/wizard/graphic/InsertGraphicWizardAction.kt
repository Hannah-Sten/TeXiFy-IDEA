package nl.hannahsten.texifyidea.action.wizard.graphic

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.PlatformDataKeys
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.fileEditor.TextEditor
import nl.hannahsten.texifyidea.action.insert.InsertTable
import nl.hannahsten.texifyidea.action.wizard.table.TableInformation
import nl.hannahsten.texifyidea.lang.LatexPackage
import nl.hannahsten.texifyidea.ui.tablecreationdialog.ColumnType
import nl.hannahsten.texifyidea.ui.tablecreationdialog.TableCreationDialogWrapper
import nl.hannahsten.texifyidea.util.*
import nl.hannahsten.texifyidea.util.files.psiFile
import java.util.*

/**
 * Action that shows a dialog with a graphic insertion wizard, and inserts the graphic as latex at the location of the
 * cursor.
 *
 * @author Hannah Schellekens
 */
class InsertGraphicWizardAction : AnAction() {

    override fun actionPerformed(e: AnActionEvent) {
        val file = e.getData(PlatformDataKeys.VIRTUAL_FILE) ?: return
        val project = e.getData(PlatformDataKeys.PROJECT) ?: return
        val editor = project.currentTextEditor() ?: return
        val document = editor.editor.document

        // Get the indentation from the current line.
        val indent = document.lineIndentationByOffset(editor.editor.caretOffset())

        // Create the dialog.
        val dialogWrapper = InsertGraphicWizardDialogWrapper()

        // If the user pressed OK, do stuff.
        if (!dialogWrapper.showAndGet()) return

        val graphicData = dialogWrapper.extractData()
    }
}
