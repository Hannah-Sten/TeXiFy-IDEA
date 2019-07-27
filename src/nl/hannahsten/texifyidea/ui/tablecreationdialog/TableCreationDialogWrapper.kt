package nl.hannahsten.texifyidea.ui.tablecreationdialog

import com.intellij.openapi.ui.DialogWrapper
import com.intellij.openapi.ui.ValidationInfo
import com.intellij.ui.ToolbarDecorator
import com.intellij.ui.components.JBLabel
import com.intellij.ui.components.JBTextField
import com.intellij.ui.table.JBTable
import nl.hannahsten.texifyidea.action.tablewizard.TableInformation
import java.awt.BorderLayout
import java.awt.Dimension
import java.awt.event.ActionEvent
import javax.swing.*
import javax.swing.border.EmptyBorder

/**
 * Wrapper that contains the table creation dialog. It validates the form when clicking the OK button.
 *
 * @param columnTypes The types of the columns of the table, see [ColumnType], always start with an empty table.
 * @param tableModel The model of the table, always start with an empty table.
 * @param tableInformation Information about the table that is needed to convert it to latex.
 *
 * UI components that have to be validated when clicking the OK button, i.e., checking if the user entered something.
 * @param table The JTable component that shows the table.
 * @param caption The text field that contains the caption for the table.
 * @param reference The text field that contains the label for the table. It has a default value "tab:" to encourage usage
 * of label conventions.
 *
 * @author Abby Berkers
 */
class TableCreationDialogWrapper(private val columnTypes: MutableList<ColumnType> = emptyList<ColumnType>().toMutableList(),
                                 private val tableModel: TableCreationTableModel = TableCreationTableModel(),
                                 var tableInformation: TableInformation = TableInformation(tableModel, columnTypes, "", ""),
                                 // Components that have to be validated when clicking the OK button.
                                 private val table: JTable = JBTable(tableModel),
                                 private val caption: JBTextField = JBTextField(),
                                 private val reference: JBTextField = JBTextField("tab:"))
    : DialogWrapper(true) {

    init {
        // Initialise the dialog, otherwise it shows as a line (i.e., infinitely small) and without any of the elements.
        init()
    }

    /**
     * Add a table column.
     *
     * @param title of the column.
     * @param typedColumnIndex is the column type of the column.
     */
    private val addColumnFun = fun(title: String, columnType: ColumnType, _: Int) {
        // Add the column to the table, with an empty cell for each row (instead of the default null).
        tableModel.addColumn(title, (0 until tableModel.rowCount).map { "" }.toTypedArray())
        // Add the column type to the list of column types.
        columnTypes.add(columnType)
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
    private val editColumnFun = fun(title: String, columnType: ColumnType, columnIndex: Int) {
        tableModel.setHeaderName(title, columnIndex)
        // Edit the column type of the edited column.
        columnTypes[columnIndex] = columnType
        tableModel.fireTableStructureChanged()
    }


    override fun createCenterPanel(): JPanel {
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

        val captionLabel = JBLabel("Caption:")
        captionLabel.labelFor = caption

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

        return panel
    }

    /**
     * When clicking OK, the wrapper will validate the form. This means that the table should at least have a header,
     * there is some text in the caption text field, and the label text field contains more than just "tab:" (or no
     * "tab:" at all, but then it should not be empty).
     */
    override fun doValidate(): ValidationInfo? {
        return if (tableModel.getColumnNames().size == 0) ValidationInfo("Table cannot be empty.", table)
        else if (caption.text.isEmpty()) ValidationInfo("Caption cannot be empty.", caption)
        else if (reference.text.isEmpty() || reference.text == "tab:") ValidationInfo("Label cannot be empty", reference)
        else {
            // 'Save' the current values in the form.
            tableInformation = TableInformation(tableModel, columnTypes, caption.text, reference.text)
            return null
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
                // Perform the usual action.
                action.actionPerformed(e)
            }
        }

        // Map the new action to the TAB key.
        actionMap.put(actionKey, actionWrapper)
    }
}