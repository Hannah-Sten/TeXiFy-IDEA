package nl.rubensten.texifyidea.action.tablewizard

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.PlatformDataKeys
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.fileEditor.TextEditor
import nl.rubensten.texifyidea.action.insert.InsertTable
import nl.rubensten.texifyidea.ui.tablecreationdialog.ColumnType
import nl.rubensten.texifyidea.ui.tablecreationdialog.TableCreationDialog
import nl.rubensten.texifyidea.util.caretOffset
import nl.rubensten.texifyidea.util.lineIndentation
import nl.rubensten.texifyidea.util.lineIndentationByOffset

/**
 * Action that shows a dialog with a table creation wizard, and inserts the table as latex at the location of the
 * cursor when clicking OK.
 */
class LatexTableWizardAction : AnAction() {
    override fun actionPerformed(e: AnActionEvent) {
        val file = e.getData(PlatformDataKeys.VIRTUAL_FILE)
        val project = e.getData(PlatformDataKeys.PROJECT)
        val editors = FileEditorManager.getInstance(project!!).selectedEditors
        val editor = editors.filter { it is TextEditor }.map { it as TextEditor }.first()
        val document = editor.editor.document

        // Get the indentation from the current line.
        val indent = document.lineIndentation(document.getLineNumber(editor.editor.caretOffset()))
        val tableTextToInsert = convertTableToLatex(TableCreationDialog().tableInformation, indent)
        InsertTable(tableTextToInsert).actionPerformed(file, project, editor)
    }

    /**
     * Convert the table information to a latex table that can be inserted into the file.
     *
     * @param tableInformation
     * @param lineIndent is the indentation of the current line, to be used on each new line.
     * @param tabIndent is the continuation indent.
     */
    private fun convertTableToLatex(tableInformation: TableInformation, lineIndent: String, tabIndent: String = "    "): String {
        fun processTableContent(indent: String): String{
            return with(tableInformation) {
                val headers = tableModel.getColumnNames()
                        .joinToString(prefix = "$indent\\toprule\n$indent", separator = " & ", postfix = " \\\\\n$indent\\midrule\n") { "\\textbf{$it}" }
                headers
            }
        }

        return with(tableInformation) {
            val openTableCommand = "\\begin{table}\n" +
                    "$lineIndent$tabIndent\\centering\n" +
                    "$lineIndent$tabIndent\\begin{tabular}{${columnTypes.toLatexColumnFormatters()}}\n"

            val content = processTableContent(indent = lineIndent + tabIndent + tabIndent)

            val closeTableCommand = "$lineIndent$tabIndent\\end{tabular}\n" +
                    "$lineIndent$tabIndent\\caption{$caption}\n" +
                    "$lineIndent$tabIndent\\label{$label}\n" +
                    "$lineIndent\\end{table}\n$lineIndent"

            openTableCommand + content + closeTableCommand
        }
    }

    /**
     * Convert the list of column types to a latex column format.
     */
    private fun List<ColumnType>.toLatexColumnFormatters(): String = joinToString(separator = "") {
        when (it) {
            ColumnType.TEXT_COLUMN -> "l"
            ColumnType.MATH_COLUMN -> "l"
            ColumnType.NUMBERS_COLUMN -> "r"
        }
    }

}