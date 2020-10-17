package nl.hannahsten.texifyidea.ui.tablecreationdialog

import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.KeyboardShortcut
import com.intellij.openapi.actionSystem.ShortcutSet
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.openapi.ui.ValidationInfo
import com.intellij.ui.AnActionButton
import com.intellij.ui.LayeredIcon
import com.intellij.ui.ToolbarDecorator
import com.intellij.ui.components.JBLabel
import com.intellij.ui.components.JBTextField
import com.intellij.ui.scale.JBUIScale.scale
import com.intellij.ui.table.JBTable
import com.intellij.util.IconUtil
import nl.hannahsten.texifyidea.action.tablewizard.TableInformation
import java.awt.*
import java.awt.event.ActionEvent
import java.awt.event.KeyEvent
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
class TableCreationDialogWrapper(
    private val columnTypes: MutableList<ColumnType> = emptyList<ColumnType>().toMutableList(),
    private val tableModel: TableCreationTableModel = TableCreationTableModel(),
    var tableInformation: TableInformation = TableInformation(tableModel, columnTypes, "", ""),
    // Components that have to be validated when clicking the OK button.
    private val table: JTable = JBTable(tableModel),
    private val caption: JBTextField = JBTextField(),
    private val reference: JBTextField = JBTextField("tab:")
) :
    DialogWrapper(true) {

    init {
        // Initialise the dialog, otherwise it shows as a line (i.e., infinitely small) and without any of the elements.
        init()
    }

    /**
     * Add a table column.
     *
     * @param title of the column.
     * @param columnType is the column type of the column.
     */
    @Suppress("KDocUnresolvedReference")
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
     * @param columnType is the index of the column type.
     * @param columnIndex is the index of the edited column in the table, starting at 0.
     */
    @Suppress("KDocUnresolvedReference", "KDocUnresolvedReference")
    private val editColumnFun = fun(title: String, columnType: ColumnType, columnIndex: Int) {
        tableModel.setHeaderName(title, columnIndex)
        // Edit the column type of the edited column.
        columnTypes[columnIndex] = columnType
        tableModel.fireTableStructureChanged()
    }

    private fun getEditColumnActionButton(): AnActionButton = object : AnActionButton("Edit column header", addText(IconUtil.getEditIcon(), "C")) {

        override fun isEnabled() = table.columnCount > 0

        override fun actionPerformed(e: AnActionEvent) {
            if (table.selectedColumn >= 0) {
                TableCreationEditColumnDialog(
                    editColumnFun,
                    table.selectedColumn,
                    table.getColumnName(table.selectedColumn),
                    columnTypes[table.selectedColumn]
                )
            }
        }
    }

    private fun getAddRowActionButton() = object : AnActionButton("Add Row", addText(IconUtil.getAddIcon(), "R")) {

        override fun isEnabled() = table.columnCount > 0

        override fun actionPerformed(e: AnActionEvent) {
            tableModel.addEmptyRow()
        }
    }

    private fun getRemoveRowActionButton() = object : AnActionButton("Remove Row", addText(IconUtil.getRemoveIcon(), "R")) {

        override fun isEnabled() = table.selectedRow > -1

        override fun actionPerformed(e: AnActionEvent) {
            tableModel.removeRow(table.selectedRow)
        }
    }

    private fun getRemoveColumnActionButton() = object : AnActionButton("Remove Column", addText(IconUtil.getRemoveIcon(), "C")) {

        override fun isEnabled() = table.selectedColumn > -1

        override fun actionPerformed(e: AnActionEvent) {
            tableModel.removeColumn(table.selectedColumn)
        }
    }

    override fun createCenterPanel(): JPanel {

        // Decorator that contains the add/remove/edit buttons.
        val decorator = ToolbarDecorator.createDecorator(table)
            .setAddAction {
                TableCreationEditColumnDialog(addColumnFun, tableModel.columnCount)
            }
            .setAddActionName("Add Column")
            .setAddIcon(addText(IconUtil.getAddIcon(), "C"))
            .addExtraAction(getRemoveColumnActionButton())
            .addExtraAction(getEditColumnActionButton())
            .addExtraAction(getAddRowActionButton())
            .addExtraAction(getRemoveRowActionButton().apply { shortcut = ShortcutSet { arrayOf(KeyboardShortcut(KeyStroke.getKeyStroke("DELETE"), null)) } })
            .createPanel()

        table.addTabCreatesNewRowAction()
        table.addEnterCreatesNewRowAction()

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

            // Help text
            val helpText = JBLabel("<html>Press tab to go to the next cell or row, press enter to go to the next row.</html>")
            helpText.foreground = Color.GRAY

            // Put help text below table
            val tablePanelContainer = JPanel(GridBagLayout())
            val constraints = GridBagConstraints()
            constraints.gridx = 0
            constraints.gridy = GridBagConstraints.RELATIVE
            tablePanelContainer.apply {
                add(tablePanel, constraints)
                add(helpText, constraints)
            }
            add(tablePanelContainer)

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
            // Add some air between components.
            add(Box.createRigidArea(Dimension(0, 8)))
            add(captionPanel)
            add(Box.createRigidArea(Dimension(0, 8)))
            add(referencePanel)
        }

        return panel
    }

    /**
     * See [IconUtil.addText].
     */
    fun addText(base: Icon, text: String, scale: Float = 7f): Icon? {
        val icon = LayeredIcon(2)
        icon.setIcon(base, 0, SwingConstants.NORTH_WEST)
        icon.setIcon(IconUtil.textToIcon(text, JLabel(), scale(scale)), 1, SwingConstants.SOUTH_EAST)
        return icon
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
                if (table.selectionModel.leadSelectionIndex == table.rowCount - 1 &&
                    table.columnModel.selectionModel.leadSelectionIndex == table.columnCount - 1
                ) {
                    tableModel.addEmptyRow()
                }
                // Perform the usual action.
                action.actionPerformed(e)
            }
        }

        // Map the new action to the TAB key.
        actionMap.put(actionKey, actionWrapper)
    }

    /**
     * Sets the action when pressing ENTER to create a new (empty) row and set the
     * selection on the first cell of the new row.
     *
     * See [addTabCreatesNewRowAction]
     */
    private fun JTable.addEnterCreatesNewRowAction() {
        val keyStroke = KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0)
        getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(keyStroke, "enter")

        val actionWrapper = object : AbstractAction() {
            override fun actionPerformed(e: ActionEvent?) {
                tableModel.addEmptyRow()

                val keyStrokeTab = KeyStroke.getKeyStroke("TAB")
                val actionKey = getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).get(keyStrokeTab)
                // Get the action to go to the next cell
                val nextCellAction = actionMap[actionKey]

                // Skip the rest of the cells in the row
                val table = this@addEnterCreatesNewRowAction
                for (i in table.columnModel.selectionModel.leadSelectionIndex until table.columnCount) {
                    nextCellAction.actionPerformed(e)
                }
            }
        }

        actionMap.put("enter", actionWrapper)
    }
}