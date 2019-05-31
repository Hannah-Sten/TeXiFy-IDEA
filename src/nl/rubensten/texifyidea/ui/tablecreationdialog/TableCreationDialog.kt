package nl.rubensten.texifyidea.ui.tablecreationdialog

import com.intellij.openapi.ui.DialogBuilder
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.ui.ToolbarDecorator
import com.intellij.ui.components.JBLabel
import com.intellij.ui.components.JBTextField
import com.intellij.ui.table.JBTable
import nl.rubensten.texifyidea.action.tablewizard.TableInformation
import java.awt.*
import java.awt.event.ActionEvent
import javax.swing.*
import javax.swing.border.EmptyBorder

class TableCreationDialog(private val columnTypes: MutableList<ColumnType> = emptyList<ColumnType>().toMutableList(),
                          private val tableModel: TableCreationTableModel = TableCreationTableModel(),
                          var tableInformation: TableInformation = TableInformation(tableModel, columnTypes, "", "")) {

    /**
     * Add a table column.
     *
     * @param title of the column.
     * @param typedColumnIndex is the column type of the column.
     */
    private val addColumnFun = fun(title: String, typedColumnIndex: Int, _: Int) {
        // Add the column to the table.
        tableModel.addColumn(title)
        // Add the column type to the list of column types.
        val selectedColumnType = ColumnType.values()[typedColumnIndex]
        columnTypes.add(selectedColumnType)
        // If table is currently empty, add one row to this new column.
        if (tableModel.columnCount == 1) tableModel.addRow(arrayOf(""))
    }

    /**
     * Edit the table column, i.e., udpate the header title and the column type.
     *
     * @param title is the new title of the header.
     * @param typedColumnIndex is the index of the column type.
     * @param columnIndex is the index of the edited column in the table, starting at 0.
     */
    private val editColumnFun = fun(title: String, typedColumnIndex: Int, columnIndex: Int) {
        tableModel.setHeaderName(title, columnIndex)
        // Edit the column type of the edited column.
        columnTypes[columnIndex] = ColumnType.values()[typedColumnIndex]
        tableModel.fireTableStructureChanged()
    }

    init {
        DialogBuilder().apply {
            setTitle("Table Creation Wizard")

            // The table.
            val table = JBTable(tableModel)
            // Decorator that contains the add/remove/edit buttons.
            val decorator = ToolbarDecorator.createDecorator(table)
                    .setAddAction {
                        TableCreationEditColumnDialog(addColumnFun, tableModel.columnCount)
                    }
                    .setAddActionName("Add Column")
                    .setRemoveAction { tableModel.removeColumn(table.selectedColumn) }
                    .setRemoveActionName("Remove Column")
                    .setEditAction {
                        TableCreationEditColumnDialog(
                                editColumnFun,
                                table.selectedColumn,
                                table.getColumnName(table.selectedColumn),
                                columnTypes[table.selectedColumn])
                    }
                    .setEditActionName("Edit Column Type")
                    .createPanel()

            table.addTabCreatesNewRowAction()

            val caption = JBTextField()
            val captionLabel = JBLabel("Caption:")
            captionLabel.labelFor = caption

            val reference = JBTextField("tab:")
            val referenceLabel = JBLabel("Label:")
            referenceLabel.labelFor = reference

            // Add all elements to the panel view.
            val panel = JPanel()
            panel.apply {
                // Add some air around the elements.
                border = EmptyBorder(8, 8, 8, 8)
                layout = BoxLayout(this, BoxLayout.Y_AXIS)

                // Create a panel for the table and its decorator.
                val tablePanel = JPanel()
                tablePanel.apply {
                    layout = BorderLayout()
                    add(JScrollPane(table), BorderLayout.WEST)
                    add(decorator, BorderLayout.EAST)
                }

                // Create a panel for the caption box and its label.
                val captionPanel = JPanel()
                captionPanel.apply {
                    layout = BoxLayout(this, BoxLayout.X_AXIS)
                    captionLabel.preferredSize = Dimension(80, captionLabel.height)
                    add(captionLabel)
                    add(caption)
                }

                // Create a panel for the label/reference box and its label.
                val referencePanel = JPanel()
                referencePanel.apply {
                    layout = BoxLayout(this, BoxLayout.X_AXIS)
                    referenceLabel.preferredSize = Dimension(80, referenceLabel.height)
                    add(referenceLabel)
                    add(reference)
                }

                // Actually add all the panels to the main panel.
                add(tablePanel)
                // Add some air between components.
                add(Box.createRigidArea(Dimension(0, 8)))
                add(captionPanel)
                add(Box.createRigidArea(Dimension(0, 8)))
                add(referencePanel)
            }
            setCenterPanel(panel)

            addOkAction()
            setOkOperation {
                dialogWrapper.close(0)
            }


            if (show() == DialogWrapper.OK_EXIT_CODE) {
                tableInformation = TableInformation(tableModel, columnTypes, caption.text, reference.text)
            }
        }
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
                    tableModel.addEmptyRow()
                }
                action.actionPerformed(e)
            }
        }
        // Map the new action to the TAB key.
        actionMap.put(actionKey, actionWrapper)
    }
}