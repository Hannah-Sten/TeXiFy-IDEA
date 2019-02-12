package nl.rubensten.texifyidea.settings

import com.intellij.openapi.options.SearchableConfigurable
import com.intellij.ui.TableUtil
import com.intellij.ui.ToolbarDecorator
import com.intellij.ui.table.JBTable
import com.intellij.util.ui.UIUtil
import java.awt.Component
import java.awt.Dimension
import java.awt.FlowLayout
import java.awt.Font
import javax.swing.*
import javax.swing.table.DefaultTableCellRenderer
import javax.swing.table.DefaultTableModel
import javax.swing.table.TableCellRenderer

/**
 * @author Ruben Schellekens, Sten Wessel
 */
class TexifyConfigurable(private val settings: TexifySettings) : SearchableConfigurable {
    private lateinit var automaticSoftWraps: JCheckBox
    private lateinit var automaticSecondInlineMathSymbol: JCheckBox
    private lateinit var automaticUpDownBracket: JCheckBox
    private lateinit var automaticItemInItemize: JCheckBox
    private lateinit var tableInfo: DefaultTableModel
    private lateinit var table: JBTable

    companion object {
        private const val NAME_LABEL = " Name of command"
        private const val POSITION_LABEL = " Position of label parameter"
        @JvmStatic fun getNameLabel() = TexifyConfigurable.NAME_LABEL
        @JvmStatic fun getPositionLabel() = TexifyConfigurable.POSITION_LABEL
    }

    override fun getId() = "TexifyConfigurable"

    override fun getDisplayName() = "TeXiFy"

    override fun createComponent() = JPanel(FlowLayout(FlowLayout.LEFT)).apply {
        add(JPanel().apply {
            layout = BoxLayout(this, BoxLayout.Y_AXIS)

            automaticSoftWraps = addCheckbox("Enable soft wraps when opening LaTeX files")
            automaticSecondInlineMathSymbol = addCheckbox("Automatically insert second '$'")
            automaticUpDownBracket = addCheckbox("Automatically insert braces around text in subscript and superscript")
            automaticItemInItemize = addCheckbox("Automatically insert '\\item' in itemize-like environments on pressing enter")

            table = addTable()
        })
        tableInfo = table.model as DefaultTableModel
        updateTableSize()
    }

    private fun JPanel.addTable() : JBTable {
        val tableInfo = MyTableModel()
        tableInfo.addColumn(TexifyConfigurable.NAME_LABEL)
        tableInfo.addColumn(TexifyConfigurable.POSITION_LABEL)
        val table = JBTable(tableInfo)
        table.intercellSpacing = Dimension(0, 0)
        table.setShowGrid(false)
        table.dragEnabled = false
        table.showHorizontalLines = false
        table.showVerticalLines = false
        table.selectionModel.selectionMode = ListSelectionModel.MULTIPLE_INTERVAL_SELECTION
        table.tableHeader.defaultRenderer = HeaderRenderer(table)

        val panel = JPanel(FlowLayout(FlowLayout.LEFT))

        val decorator = ToolbarDecorator.createDecorator(table)
                .setAddAction { addCommand(tableInfo) }
                .setRemoveAction { removeCommand(table) }
                .setEditAction { editCommand(table, tableInfo) }
                .createPanel()
        panel.apply { add(decorator) }
        add(panel)
        return table
    }

    private fun addCommand(tableInfo: MyTableModel) {
        val dialog = TexifyDefineLabelingCommand("", 1)
        if (dialog.showAndGet()) {
            tableInfo.addRow(arrayOf(dialog.getMyCommandName(), dialog.getMyCommandPosition()))
            updateTableSize()
        }
    }

    private fun editCommand(table: JBTable, tableInfo: MyTableModel) {
        val row = table.selectedRow
        val name = tableInfo.getValueAt(row, 0) as String
        val position = tableInfo.getValueAt(row, 1) as Int
        val dialog = TexifyDefineLabelingCommand(name, position)
        if (dialog.showAndGet()) {
            tableInfo.setValueAt(dialog.getMyCommandName(), row, 0)
            tableInfo.setValueAt(dialog.getMyCommandPosition(), row, 1)
        }
    }

    private fun removeCommand(table: JTable) {
        TableUtil.removeSelectedItems(table)
        updateTableSize()
    }

    private fun updateTableSize() {
        val fontMetrics = table.getFontMetrics(UIManager.getFont("Table.font").deriveFont(Font.BOLD))

        var nameWidth = fontMetrics.stringWidth(TexifyConfigurable.NAME_LABEL) + UIUtil.DEFAULT_HGAP
        var positionWidth = fontMetrics.stringWidth(TexifyConfigurable.POSITION_LABEL)
        val tableHeight = table.rowHeight * (table.rowCount + 3)

        for (i in 0 until tableInfo.rowCount) {
            val label = tableInfo.getValueAt(i, 0) as String
            val labelWidth = fontMetrics.stringWidth(label) + UIUtil.DEFAULT_HGAP

            val position = tableInfo.getValueAt(i, 1) as Int
            val actualPositionWidth = fontMetrics.stringWidth("$position")

            nameWidth = if (nameWidth > labelWidth) nameWidth else labelWidth
            positionWidth = if (positionWidth > actualPositionWidth) positionWidth else actualPositionWidth
        }

        table.columnModel.getColumn(0).preferredWidth = nameWidth
        table.columnModel.getColumn(1).preferredWidth = positionWidth

        table.preferredScrollableViewportSize = Dimension(nameWidth + positionWidth + UIUtil.DEFAULT_HGAP,
                tableHeight)
    }

    private fun JPanel.addCheckbox(message: String): JCheckBox {
        val checkBox = JCheckBox(message)
        add(JPanel(FlowLayout(FlowLayout.LEFT)).apply {
            add(checkBox)
        })
        return checkBox
    }

    override fun isModified(): Boolean {
        return automaticSoftWraps.isSelected != settings.automaticSoftWraps
                || automaticSecondInlineMathSymbol.isSelected != settings.automaticSecondInlineMathSymbol
                || automaticUpDownBracket.isSelected != settings.automaticUpDownBracket
                || automaticItemInItemize.isSelected != settings.automaticItemInItemize
                || commandsModified()
    }

    override fun apply() {
        settings.automaticSoftWraps = automaticSoftWraps.isSelected
        settings.automaticSecondInlineMathSymbol = automaticSecondInlineMathSymbol.isSelected
        settings.automaticUpDownBracket = automaticUpDownBracket.isSelected
        settings.automaticItemInItemize = automaticItemInItemize.isSelected

        val names = settings.labelCommands.keys.toMutableList()
        val removeRows = mutableListOf<Int>()

        for (i in 0 until tableInfo.rowCount) {
            val command = tableInfo.getValueAt(i, 0) as String
            val pos = tableInfo.getValueAt(i, 1)
            var position = 0
            if (pos is Int) {
                position = pos
            }
            else if (pos is String){
                val positionNull = pos.toIntOrNull()
                if (positionNull != null && positionNull.toString() == pos) {
                    position = positionNull
                }
            }
            if (position > 0 && command != "") {
                settings.labelCommands[command] = position
                names.remove(tableInfo.getValueAt(i, 0) as String)
            }
            else {
                removeRows.add(i)
            }
        }
        names.forEach{settings.labelCommands.remove(it)}
        removeRows.forEach { tableInfo.removeRow(it) }
    }

    override fun reset() {
        automaticSoftWraps.isSelected = settings.automaticSoftWraps
        automaticSecondInlineMathSymbol.isSelected = settings.automaticSecondInlineMathSymbol
        automaticUpDownBracket.isSelected = settings.automaticUpDownBracket
        automaticItemInItemize.isSelected = settings.automaticItemInItemize
        var i = 0
        settings.labelCommands.forEach { command, position -> addOrUpdateRow(i++, command, position) }
        while (i < tableInfo.rowCount) {
            tableInfo.removeRow(i)
        }
        updateTableSize()
    }

    private fun addOrUpdateRow(row : Int, command: String, position: Int) {
        if (tableInfo.rowCount > row) {
            tableInfo.removeRow(row)
            tableInfo.insertRow(row, arrayOf(command, position))
        }
        else {
            tableInfo.addRow(arrayOf(command, position))
        }
    }

    private fun commandsModified() : Boolean {
        if (tableInfo.rowCount != settings.labelCommands.size) {
            return true
        }
        for (i in 0 until tableInfo.rowCount) {
            if (!settings.labelCommands.containsKey(tableInfo.getValueAt(i, 0) as String) ||
                    settings.labelCommands[tableInfo.getValueAt(i, 0) as String] != tableInfo.getValueAt(i, 1)) {
                return true
            }
        }
        return false
    }

    private class HeaderRenderer(table: JTable) : TableCellRenderer {
        val renderer : DefaultTableCellRenderer = table.tableHeader.defaultRenderer as DefaultTableCellRenderer

        init {
            renderer.border = BorderFactory.createCompoundBorder(table.tableHeader.border,
                    BorderFactory.createEmptyBorder(0, 100, 0, 0))
            renderer.horizontalAlignment = JLabel.LEFT
        }

        override fun getTableCellRendererComponent(table: JTable?, value: Any?, isSelected: Boolean, hasFocus: Boolean,
                                                   row: Int, column: Int): Component {
            return renderer.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column)
        }

    }
}
