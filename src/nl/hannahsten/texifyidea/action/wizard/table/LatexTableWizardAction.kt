package nl.hannahsten.texifyidea.action.wizard.table

import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.PlatformDataKeys
import com.intellij.openapi.project.Project
import nl.hannahsten.texifyidea.util.caretOffset
import nl.hannahsten.texifyidea.util.currentTextEditor
import nl.hannahsten.texifyidea.util.files.isLatexFile
import nl.hannahsten.texifyidea.util.lineIndentationByOffset
import java.util.*

/**
 * Action that shows a dialog with a table creation wizard, and inserts the table as latex at the location of the
 * cursor when clicking OK.
 *
 * @author Abby Berkers
 */
class LatexTableWizardAction : AnAction() {

    fun executeAction(project: Project, defaultDialogWrapper: TableCreationDialogWrapper? = null): String {
        val editor = project.currentTextEditor() ?: return ""
        val document = editor.editor.document

        // Get the indentation from the current line.
        val indent = document.lineIndentationByOffset(editor.editor.caretOffset())

        // Create the dialog.
        val dialogWrapper = defaultDialogWrapper ?: TableCreationDialogWrapper()

        // If the user pressed OK, do stuff.
        if (dialogWrapper.showAndGet()) {
            // Get the table information from the dialog, and convert it to latex.
            with(dialogWrapper.tableInformation) {
                return convertTableToLatex(indent)
            }

            // todo the code that executed the action is removed, so this executeAction does not execute an action?
            // todo check if booktabs package is inserted correctly wherever this code moved to
        }

        return ""
    }

    override fun actionPerformed(e: AnActionEvent) {
        val project = e.getData(PlatformDataKeys.PROJECT) ?: return
        executeAction(project)
    }

    override fun update(e: AnActionEvent) {
        super.update(e)

        val file = e.getData(PlatformDataKeys.PSI_FILE)
        val shouldDisplayMenu = file?.isLatexFile() == true
        e.presentation.isVisible = shouldDisplayMenu
    }

    override fun getActionUpdateThread() = ActionUpdateThread.BGT

    /**
     * Local function to process the contents of the table, i.e. the header text and table entries. The indentation
     * is the same throughout the table contents.
     */
    fun TableInformation.processTableContent(indent: String): String {

        val headers = tableModel.getColumnNames()
            .joinToString(
                prefix = "$indent\\toprule\n$indent",
                separator = " & ",
                postfix = " \\\\\n$indent\\midrule\n"
            ) { "\\textbf{$it}" }

        val rows =
            tableModel.dataVector.joinToString(separator = "\n", postfix = "\n$indent\\bottomrule\n") { row ->
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
        return headers + rows
    }

    /**
     * Convert the table information to a latex table that can be inserted into the file.
     *
     * @param lineIndent
     *          The indentation of the current line, to be used on each new line.
     * @param tabIndent
     *          The continuation indent.
     */
    private fun TableInformation.convertTableToLatex(
        lineIndent: String,
        tabIndent: String = "    "
    ): String {

        // The tex(t) for a table consists of three parts: the open commands, the actual content, and the closing commands
        // (this includes the caption and label).
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

        return openTableCommand + content + closeTableCommand
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
