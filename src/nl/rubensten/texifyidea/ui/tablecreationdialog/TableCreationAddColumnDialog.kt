package nl.rubensten.texifyidea.ui.tablecreationdialog

import com.intellij.openapi.ui.DialogBuilder
import com.intellij.openapi.ui.DialogWrapper
import javax.swing.JLabel
import javax.swing.JPanel
import javax.swing.JTextField
import javax.swing.table.DefaultTableModel

/**
 * Dialog to add a new column to the table.
 */
class TableCreationAddColumnDialog(private val tableModel: DefaultTableModel) {
    init {
        DialogBuilder().apply {
            val columnNameField = JTextField()
            val columnNameLabel = JLabel("Column name")
            columnNameLabel.labelFor = columnNameField

            val panel = JPanel()
            panel.add(columnNameLabel)
            panel.add(columnNameField)
            setCenterPanel(panel)

            addOkAction()
            setOkOperation {
                dialogWrapper.close(0)
            }

            if (show() == DialogWrapper.OK_EXIT_CODE) {
                // TODO if table is currently empty, add one row to this new column.
                tableModel.addColumn(columnNameField.text)
            }
        }
    }
}