package nl.hannahsten.texifyidea.action.tablewizard

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.PlatformDataKeys
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.fileEditor.TextEditor
import nl.hannahsten.texifyidea.action.insert.InsertTable
import nl.hannahsten.texifyidea.lang.Package
import nl.hannahsten.texifyidea.ui.tablecreationdialog.ColumnType
import nl.hannahsten.texifyidea.ui.tablecreationdialog.TableCreationDialogWrapper
import nl.hannahsten.texifyidea.util.*
import java.util.*

/**
 * Action that shows a dialog with a table creation wizard, and inserts the table as latex at the location of the
 * cursor when clicking OK.
 *
 * @author Abby Berkers
 */
class LatexTableWizardAction : AnAction() {
    override fun actionPerformed(e: AnActionEvent) {
        val file = e.getData(PlatformDataKeys.VIRTUAL_FILE)
        val project = e.getData(PlatformDataKeys.PROJECT)
        val editors = FileEditorManager.getInstance(project!!).selectedEditors
        val editor = editors.first { it is TextEditor } as TextEditor
        val document = editor.editor.document

        // Get the indentation from the current line.
        val indent = document.lineIndentation(document.getLineNumber(editor.editor.caretOffset()))

        // Create the dialog.
        val dialogWrapper = TableCreationDialogWrapper()
        // If the user pressed OK, do stuff.
        if (dialogWrapper.showAndGet()) {

            // Get the table information from the dialog, and convert it to latex.
            val tableTextToInsert = convertTableToLatex(dialogWrapper.tableInformation, indent)

            // Use an insert action to insert the table.
            InsertTable(tableTextToInsert).actionPerformed(file, project, editor)

            // Insert the booktabs package.
            WriteCommandAction.runWriteCommandAction(project,
                    "Insert table",
                    "LaTeX",
                    Runnable { file!!.psiFile(project)!!.insertUsepackage(Package.BOOKTABS) },
                    file!!.psiFile(project)
            )
        }

    }

    /**
     * Convert the table information to a latex table that can be inserted into the file.
     *
     * @param tableInformation
     * @param lineIndent is the indentation of the current line, to be used on each new line.
     * @param tabIndent is the continuation indent.
     */
    private fun convertTableToLatex(tableInformation: TableInformation, lineIndent: String, tabIndent: String = "    "): String {
        /**
         * Local function to process the contents of the table, i.e. the header text and table entries. The indentation
         * is the same throughout the table contents.
         */
        fun processTableContent(indent: String): String {
            return with(tableInformation) {
                val headers = tableModel.getColumnNames()
                        .joinToString(
                                prefix = "$indent\\toprule\n$indent",
                                separator = " & ",
                                postfix = " \\\\\n$indent\\midrule\n"
                        ) { "\\textbf{$it}" }

                val rows = tableModel.dataVector.joinToString(separator = "\n", postfix = "\n$indent\\bottomrule\n") { row ->
                    (row as Vector<*>).joinToString(
                            prefix = indent,
                            separator = " & ",
                            postfix = " \\\\"
                    ) {
                        // Enclose with $ if the type of this column is math.
                        val index = row.indexOf(it)
                        val encloseWith = if (columnTypes[index] == ColumnType.MATH_COLUMN) "$" else ""
                        encloseWith + it.toString() + encloseWith
                    }
                }
                headers + rows
            }
        }

        // The tex(t) for a table consists of three parts: the open commands, the actual content, and the closing commands
        // (this includes the caption and label).
        return with(tableInformation) {
            val openTableCommand = "\\begin{table}\n" +
                    "$lineIndent$tabIndent\\centering\n" +
                    // Everything within the table command gets an extra indent.
                    "$lineIndent$tabIndent\\begin{tabular}{${columnTypes.toLatexColumnFormatters()}}\n"

            // The content has to be indented once more.
            val content = processTableContent(indent = lineIndent + tabIndent + tabIndent)

            val closeTableCommand = "$lineIndent$tabIndent\\end{tabular}\n" +
                    "$lineIndent$tabIndent\\caption{$caption}\n" +
                    "$lineIndent$tabIndent\\label{$label}\n" +
                    "$lineIndent\\end{table}\n" +
                    lineIndent // Indentation on the last line so we can continue typing there.

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
