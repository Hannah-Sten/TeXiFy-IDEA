package nl.rubensten.texifyidea.action

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.PlatformDataKeys
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.fileEditor.TextEditor
import nl.rubensten.texifyidea.action.insert.InsertTable
import nl.rubensten.texifyidea.ui.tablecreationdialog.TableCreationDialog

/**
 * Action that shows a dialog with a table creation wizard, and inserts the table as latex at the location of the
 * cursor when clicking OK.
 */
class LatexTableWizardAction : AnAction() {
    override fun actionPerformed(e: AnActionEvent) {
        val file = e.getData(PlatformDataKeys.VIRTUAL_FILE)
        val project = e.getData(PlatformDataKeys.PROJECT)
        val editors = FileEditorManager.getInstance(project!!).selectedEditors
        val editor = editors.filter { it is TextEditor}.map { it as TextEditor }.first()

        val tableTextToInsert = TableCreationDialog().tableAsLatex
        InsertTable(tableTextToInsert).actionPerformed(file, project, editor)
    }

}