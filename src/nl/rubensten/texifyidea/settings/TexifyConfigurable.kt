package nl.rubensten.texifyidea.settings

import com.intellij.openapi.options.SearchableConfigurable
import com.intellij.ui.TableUtil
import com.intellij.ui.ToolbarDecorator
import com.intellij.ui.table.JBTable
import java.awt.Component
import java.awt.Dimension
import java.awt.FlowLayout
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
    private lateinit var table: DefaultTableModel

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
    }

    private fun JPanel.addTable() : DefaultTableModel {
        val tableInfo = MyTableModel()
        tableInfo.addColumn(" Name of command")
        tableInfo.addColumn(" Position of label parameter")
        val table = JBTable(tableInfo)
        table.intercellSpacing = Dimension(0, 0)
        table.setShowGrid(false)
        table.dragEnabled = false
        table.showHorizontalLines = false
        table.showVerticalLines = false
        table.selectionModel.selectionMode = ListSelectionModel.MULTIPLE_INTERVAL_SELECTION
        table.tableHeader.defaultRenderer = HeaderRenderer(table)

        val panel = JPanel(FlowLayout(FlowLayout.LEFT))
        panel.preferredSize = Dimension(400, 10 * table.rowHeight)

        val decorator = ToolbarDecorator.createDecorator(table)
                .setAddAction { addCommand(tableInfo) }
                .setRemoveAction { removeCommand(table) }
                .setEditAction { editCommand(table, tableInfo) }
                .createPanel()
        panel.add(decorator)
        add(panel)
        return tableInfo
    }

    private fun addCommand(tableInfo: MyTableModel) {
        val dialog = TexifyDefineLabelingCommand("", 1)
        if (dialog.showAndGet()) {
            tableInfo.addRow(arrayOf(dialog.getMyCommandName(), dialog.getMyCommandPosition()))
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

        for (i in 0 until table.rowCount) {
            val command = table.getValueAt(i, 0) as String
            val pos = table.getValueAt(i, 1)
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
                names.remove(table.getValueAt(i, 0) as String)
            }
            else {
                removeRows.add(i)
            }
        }
        names.forEach{settings.labelCommands.remove(it)}
        removeRows.forEach { table.removeRow(it) }
    }

    override fun reset() {
        automaticSoftWraps.isSelected = settings.automaticSoftWraps
        automaticSecondInlineMathSymbol.isSelected = settings.automaticSecondInlineMathSymbol
        automaticUpDownBracket.isSelected = settings.automaticUpDownBracket
        automaticItemInItemize.isSelected = settings.automaticItemInItemize
        var i = 0
        settings.labelCommands.forEach { command, position -> addOrUpdateRow(i++, command, position) }
        while (i < table.rowCount) {
            table.removeRow(i)
        }
    }

    private fun addOrUpdateRow(row : Int, command: String, position: Int) {
        if (table.rowCount > row) {
            table.removeRow(row)
            table.insertRow(row, arrayOf(command, position))
        }
        else {
            table.addRow(arrayOf(command, position))
        }
    }

    private fun commandsModified() : Boolean {
        if (table.rowCount != settings.labelCommands.size) {
            return true
        }
        for (i in 0 until table.rowCount) {
            if (!settings.labelCommands.containsKey(table.getValueAt(i, 0) as String) ||
                    settings.labelCommands[table.getValueAt(i, 0) as String] != table.getValueAt(i, 1)) {
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
