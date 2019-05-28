package nl.rubensten.texifyidea.ui.tablecreationdialog

import com.intellij.openapi.ui.DialogBuilder
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.ui.ToolbarDecorator
import com.intellij.ui.table.JBTable
import java.awt.event.ActionEvent
import javax.swing.*
import javax.swing.table.DefaultTableModel

class TableCreationDialog(var tableAsLatex: String? = "",
                          private val columnTypes: MutableList<ColumnType> = emptyList<ColumnType>().toMutableList(),
                          private val tableModel: TableCreationTableModel = TableCreationTableModel()) {
    init {
        DialogBuilder().apply {
            setTitle("Table Creation Wizard")

            // Button to add another column.
            val addColumnButton = JButton("Add column")
            addColumnButton.addActionListener { TableCreationAddColumnDialog(tableModel, columnTypes) }


            // The table.
            val table = JBTable(tableModel)
            val decorator = ToolbarDecorator.createDecorator(table)
                    .setAddAction { TableCreationAddColumnDialog(tableModel, columnTypes) }
                    .setAddActionName("Add Column")
                    .setRemoveAction { tableModel.removeColumn(table.selectedColumn) }
                    .setRemoveActionName("Remove Column")
                    .setEditAction {  }
                    .setEditActionName("Edit Column Type")
                    .createPanel()

            table.addTabCreatesNewRowAction()

            // Add all elements to the panel view.
            // TODO beautify gui
            val panel = JPanel()
            panel.add(addColumnButton)
            panel.add(JScrollPane(table))
            panel.add(decorator)
            setCenterPanel(panel)

            addOkAction()
            setOkOperation {
                dialogWrapper.close(0)
            }
            if (show() == DialogWrapper.OK_EXIT_CODE) {
                // TODO convert the table to latex
                tableAsLatex = columnTypes.toString()
            }
        }
    }

    /**
     * Adds an empty row to the table.
     */
    private fun addEmptyRow() {
        val emptyRow = (0 until tableModel.columnCount).map { "" }.toTypedArray()
        tableModel.addRow(emptyRow)
    }

    /**
     * Sets the action when pressing TAB on the last cell in the last row to create a new (empty) row and set the
     * selection on the first cell of the new row.
     */
    private fun JTable.addTabCreatesNewRowAction() {
        // Get the key stroke for pressing TAB.
        val keyStroke = KeyStroke.getKeyStroke("TAB")
        val actionKey = getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).get(keyStroke)
        // Get the action that currently is under the TAB key.
        val action = actionMap[actionKey]

        val actionWrapper = object : AbstractAction() {
            override fun actionPerformed(e: ActionEvent?) {
                val table = this@addTabCreatesNewRowAction
                // When we're in the last column of the last row, add a new row before calling the usual action.
                if (table.selectionModel.leadSelectionIndex == table.rowCount - 1
                        && table.columnModel.selectionModel.leadSelectionIndex == table.columnCount - 1) {
                    addEmptyRow()
                }
                action.actionPerformed(e)
            }
        }
        // Map the new action to the TAB key.
        actionMap.put(actionKey, actionWrapper)
    }
}