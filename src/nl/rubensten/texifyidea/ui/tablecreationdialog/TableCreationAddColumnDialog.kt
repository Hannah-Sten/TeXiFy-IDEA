package nl.rubensten.texifyidea.ui.tablecreationdialog

import com.intellij.openapi.ui.DialogBuilder
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.ui.components.JBLabel
import com.intellij.ui.components.JBTextField
import javax.swing.JComboBox
import javax.swing.JPanel
import javax.swing.table.DefaultTableModel

/**
 * Dialog to add a new column to the table.
 */
class TableCreationAddColumnDialog(private val tableModel: DefaultTableModel,
                                   private val columnTypeArray: MutableList<ColumnType>) {
    init {
        DialogBuilder().apply {
            // Text field for the name of the column.
            val columnNameField = JBTextField()
            val columnNameLabel = JBLabel("Column name")
            columnNameLabel.labelFor = columnNameField

            val columnType = JComboBox(ColumnType.values().map { it.displayName }.toTypedArray())
            val columnTypeLabel = JBLabel("Column type")
            columnTypeLabel.labelFor = columnType

            // Add UI elements.
            val panel = JPanel()
            panel.apply {
                add(columnNameLabel)
                add(columnNameField)
                add(columnTypeLabel)
                add(columnType)
            }
            setCenterPanel(panel)
            setPreferredFocusComponent(columnNameField)

            addOkAction()
            setOkOperation {
                dialogWrapper.close(0)
            }

            if (show() == DialogWrapper.OK_EXIT_CODE) {
                // Add the column to the table.
                tableModel.addColumn(columnNameField.text)
                // Add the column type to the list of column types.
                val selectedColumnType = ColumnType.values()[columnType.selectedIndex]
                columnTypeArray.add(selectedColumnType)
                // If table is currently empty, add one row to this new column.
                if (tableModel.columnCount == 1) tableModel.addRow(arrayOf(""))
            }
        }
    }
}