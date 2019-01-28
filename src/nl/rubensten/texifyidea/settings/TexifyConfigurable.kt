package nl.rubensten.texifyidea.settings

import com.intellij.openapi.options.SearchableConfigurable
import com.intellij.ui.table.JBTable
import java.awt.FlowLayout
import javax.swing.*
import javax.swing.table.DefaultTableModel

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
        val tableInfo = DefaultTableModel()
        tableInfo.addColumn("Name of command")
        tableInfo.addColumn("Position of label parameter")
        val table = JBTable(tableInfo)

        add(JPanel(FlowLayout(FlowLayout.LEFT)).apply { add(JScrollPane(table)) })
        return tableInfo
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
        for (i in 0 until table.rowCount) {
            addOrUpdateStoredRow(i, table.getValueAt(i, 0), table.getValueAt(i, 1))
        }
    }

    override fun reset() {
        automaticSoftWraps.isSelected = settings.automaticSoftWraps
        automaticSecondInlineMathSymbol.isSelected = settings.automaticSecondInlineMathSymbol
        automaticUpDownBracket.isSelected = settings.automaticUpDownBracket
        automaticItemInItemize.isSelected = settings.automaticItemInItemize
        for (i in 0 until settings.labelCommands.size) {
            addOrUpdateRow(i, settings.labelCommands[i])
        }
    }

    private fun addOrUpdateStoredRow(row : Int, command : Any, position : Any) {
        if (settings.labelCommands.size > row) {
            settings.labelCommands[row] = arrayOf(command, position)
        }
        else {
            settings.labelCommands.plus(arrayOf(command, position))
        }
    }

    private fun addOrUpdateRow(row : Int, data : Array<Any>) {
        if (table.rowCount > row) {
            table.removeRow(row)
            table.insertRow(row, data)
        }
        else {
            table.addRow(data)
        }
    }

    private fun commandsModified() : Boolean {
        if (table.rowCount != settings.labelCommands.size) {
            return true
        }
        for (i in 0 until table.rowCount) {
            if (table.getValueAt(i, 0) != settings.labelCommands[i][0] ||
                    table.getValueAt(i, 1) != settings.labelCommands[i][1]) {
                return true
            }
        }
        return false
    }
}
