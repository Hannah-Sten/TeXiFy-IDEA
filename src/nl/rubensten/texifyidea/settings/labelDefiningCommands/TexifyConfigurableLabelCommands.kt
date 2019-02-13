package nl.rubensten.texifyidea.settings.labelDefiningCommands

import com.intellij.ui.TableUtil
import com.intellij.ui.ToolbarDecorator
import com.intellij.ui.table.JBTable
import com.intellij.util.ui.UIUtil
import nl.rubensten.texifyidea.settings.TexifySettings
import java.awt.*
import java.awt.event.ActionListener
import javax.swing.*
import javax.swing.table.DefaultTableCellRenderer
import javax.swing.table.TableCellRenderer

/**
 * Excluded table for the settings page to get smaller and more meaningful classes
 */
class TexifyConfigurableLabelCommands(private val settings: TexifySettings) {
    private var table: JBTable
    private val tableInfo: MyTableModel = MyTableModel()
    private val tablePanel = JPanel(GridBagLayout())

    companion object {
        private const val NAME_LABEL = " Name of command"
        private const val POSITION_LABEL = " Position of label parameter"

        private const val NAME_LABEL_WIDTH = 200
        private const val POSITION_LABEL_WIDTH = 150
        private const val EMPTY_ROWS_TO_DISPLAY = 3
    }

    /**
     * create the table and all such things
     */
    init {
        tableInfo.addColumn(NAME_LABEL)
        tableInfo.addColumn(POSITION_LABEL)
        table = LabelCommandSettingsTable(tableInfo, ActionListener { addCommand() })
        table.intercellSpacing = Dimension(0, 0)
        table.setShowGrid(false)
        table.dragEnabled = false
        table.showHorizontalLines = false
        table.showVerticalLines = false
        table.selectionModel.selectionMode = ListSelectionModel.MULTIPLE_INTERVAL_SELECTION
        table.tableHeader.defaultRenderer = HeaderRenderer(table)

        val gridBag = GridBagConstraints()
        gridBag.anchor = GridBagConstraints.LINE_START
        gridBag.insets = Insets(UIUtil.DEFAULT_VGAP, UIUtil.DEFAULT_HGAP, 0, 0)
        gridBag.fill = GridBagConstraints.VERTICAL
        // define the buttons for the actions
        val decorator = ToolbarDecorator.createDecorator(table)
                .setAddAction { addCommand() }
                .setRemoveAction { removeCommand(table) }
                .setEditAction { editCommand(table, tableInfo) }
                .createPanel()

        val label = JLabel("Only required parameters count for the position", SwingConstants.LEFT)
        label.foreground = Color.GRAY

        tablePanel.apply {
            gridBag.gridx = 0
            gridBag.gridy = 0
            add(decorator, gridBag)
            gridBag.gridx = 0
            gridBag.gridy = 1
            add(label, gridBag)
        }

        updateTableSize()
    }

    /**
     * show dialog to add a command and after correct input add it to the table
     */
    private fun addCommand() {
        val dialog = TexifyDefineLabelingCommand("", 1)
        if (dialog.showAndGet()) {
            tableInfo.addRow(arrayOf(dialog.getMyCommandName(), dialog.getMyCommandPosition()))
            updateTableSize()
        }
    }

    /**
     * show the same dialog but with the selected values and update row
     */
    private fun editCommand(table: JBTable, tableInfo: MyTableModel) {
        val row = table.selectedRow
        val name = tableInfo.getValueAt(row, 0) as String
        val position = tableInfo.getValueAt(row, 1) as Int
        val dialog = TexifyDefineLabelingCommand(name, position)
        if (dialog.showAndGet()) {
            tableInfo.setValueAt(dialog.getMyCommandName(), row, 0)
            tableInfo.setValueAt(dialog.getMyCommandPosition(), row, 1)
        }
        updateTableSize()
    }

    /**
     * remove currently selected row
     */
    private fun removeCommand(jTable: JBTable) {
        TableUtil.removeSelectedItems(jTable)
        updateTableSize()
    }

    /**
     * update the size of the table, to fit all the content
     */
    private fun updateTableSize() {
        val fontMetrics = table.getFontMetrics(UIManager.getFont("Table.font").deriveFont(Font.BOLD))

        // set minimum width for each column
        var nameWidth = NAME_LABEL_WIDTH
        var positionWidth = POSITION_LABEL_WIDTH
        val tableHeight = table.rowHeight * (table.rowCount + EMPTY_ROWS_TO_DISPLAY)

        // check the width of each column in each row, to display all commands and positions in full length
        for (i in 0 until tableInfo.rowCount) {
            val label = tableInfo.getValueAt(i, 0) as String
            val labelWidth = fontMetrics.stringWidth(label) + UIUtil.DEFAULT_HGAP

            val position = tableInfo.getValueAt(i, 1) as Int
            val actualPositionWidth = fontMetrics.stringWidth("$position")

            nameWidth = if (nameWidth > labelWidth) nameWidth else labelWidth
            positionWidth = if (positionWidth > actualPositionWidth) positionWidth else actualPositionWidth
        }

        // set width for each column
        table.columnModel.getColumn(0).preferredWidth = nameWidth
        table.columnModel.getColumn(1).preferredWidth = positionWidth

        // set the size of the table
        table.preferredScrollableViewportSize = Dimension(nameWidth + positionWidth + UIUtil.DEFAULT_HGAP,
                tableHeight)
    }

    /**
     * reset the table to the currently stored values
     */
    fun reset() {
        while (tableInfo.rowCount > 0) {
            tableInfo.removeRow(0)
        }
        settings.labelCommands.forEach { command, position -> tableInfo.addRow(arrayOf(command, position)) }
        updateTableSize()
    }

    /**
     * save the currently displayed settings
     */
    fun apply() {
        val commands = settings.labelCommands.keys.toMutableSet()
        // add or update each specified command
        for (i in 0 until tableInfo.rowCount) {
            val command = tableInfo.getValueAt(i, 0) as String
            val position = tableInfo.getValueAt(i, 1) as Int

            settings.labelCommands[command] = position

            commands.remove(command)
        }
        commands.forEach { settings.labelCommands.remove(it) }
    }

    /**
     * check if the table is modified
     */
    fun isModified(): Boolean {
        if (tableInfo.rowCount != settings.labelCommands.size) {
            return true
        }
        for (i in 0 until tableInfo.rowCount) {
            val command = tableInfo.getValueAt(i, 0) as String
            val position = tableInfo.getValueAt(i, 1) as Int

            if (!settings.labelCommands.containsKey(command) || settings.labelCommands[command] != position) {
                return true
            }
        }
        return false
    }

    /**
     * return the panel which holds the table to display it
     */
    fun getTable() = tablePanel

    /**
     * own renderer for the table header to display them left aligned
     */
    private class HeaderRenderer(table: JTable) : TableCellRenderer {
        val renderer : DefaultTableCellRenderer = table.tableHeader.defaultRenderer as DefaultTableCellRenderer

        init {
            renderer.horizontalAlignment = JLabel.LEFT
        }

        override fun getTableCellRendererComponent(table: JTable?, value: Any?, isSelected: Boolean, hasFocus: Boolean,
                                                   row: Int, column: Int): Component {
            return renderer.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column)
        }
    }
}
