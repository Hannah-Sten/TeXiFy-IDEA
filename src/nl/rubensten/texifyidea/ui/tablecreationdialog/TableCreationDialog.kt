package nl.rubensten.texifyidea.ui.tablecreationdialog

import com.intellij.openapi.ui.DialogBuilder
import com.intellij.openapi.ui.DialogWrapper
import javax.swing.JButton
import javax.swing.JPanel
import javax.swing.JScrollPane
import javax.swing.JTable
import javax.swing.table.DefaultTableModel

class TableCreationDialog(var tableAsLatex: String? = "bloop",
                          private val tableModel: DefaultTableModel = DefaultTableModel()) {
    init {
        DialogBuilder().apply {
            setTitle("Table Creation Wizard")

            // Button to add another column.
            val addColumnButton = JButton("Add column")
            addColumnButton.addActionListener { TableCreationAddColumnDialog(tableModel) }

            // The table.
            val table = JTable(tableModel)

            // Add all elements to the panel view.
            // TODO beautify gui
            val panel = JPanel()
            panel.add(addColumnButton)
            panel.add(JScrollPane(table))
            setCenterPanel(panel)

            addOkAction()
            setOkOperation {
                dialogWrapper.close(0)
            }
            if (show() == DialogWrapper.OK_EXIT_CODE) {
                // TODO convert the table to latex
                tableAsLatex = "tex table"
            }
        }
    }

}