package nl.hannahsten.texifyidea.ui.tablecreationdialog

import com.intellij.openapi.ui.DialogBuilder
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.ui.components.JBLabel
import com.intellij.ui.components.JBTextField
import javax.swing.JComboBox
import javax.swing.JPanel

/**
 * Dialog to add a new column to the table.
 *
 * @param onOkFunction The function to execute when clicking the OK button.
 * @param editingColumn The index of the column being edited.
 * @param columnName The name of the column that is being edited. Default is the empty string, the title of a column that does
 *          not yet exist.
 * @param columnType The [ColumnType] of the column that is being edited. Default is a text column.
 *
 * @author Abby Berkers
 */
class TableCreationEditColumnDialog(
        private val onOkFunction: (String, ColumnType, Int) -> Unit,
        private val editingColumn: Int,
        private val columnName: String = "",
        private val columnType: ColumnType = ColumnType.TEXT_COLUMN) {

    init {
        DialogBuilder().apply {
            // Text field for the name of the column, with the old name of the editing column filled in.
            val columnNameField = JBTextField(columnName)
            val columnNameLabel = JBLabel("Column name")
            columnNameLabel.labelFor = columnNameField

            // A combobox to select the column type.
            val columnTypeComboBox = JComboBox(ColumnType.values().map { it.displayName }.toTypedArray())
            // Select the old type of the editing column.
            columnTypeComboBox.selectedIndex = ColumnType.values().indexOf(columnType)
            val columnTypeLabel = JBLabel("Column type")
            columnTypeLabel.labelFor = columnTypeComboBox

            // Add UI elements.
            val panel = JPanel()
            panel.apply {
                add(columnNameLabel)
                add(columnNameField)
                add(columnTypeLabel)
                add(columnTypeComboBox)
            }
            setCenterPanel(panel)
            setPreferredFocusComponent(columnNameField)

            addOkAction()
            setOkOperation {
                dialogWrapper.close(0)
            }

            if (show() == DialogWrapper.OK_EXIT_CODE) {
                onOkFunction(columnNameField.text, ColumnType.values()[columnTypeComboBox.selectedIndex], editingColumn)
            }
        }
    }
}