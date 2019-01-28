package nl.rubensten.texifyidea.settings

import com.intellij.openapi.options.SearchableConfigurable
import com.intellij.ui.table.JBTable
import java.awt.FlowLayout
import java.awt.event.ActionListener
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
    private lateinit var newLineButton: JButton

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
            newLineButton = addButton()
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

    private fun JPanel.addButton() : JButton {
        val button = JButton("New line")
        button.addActionListener {
            if(table.getValueAt(table.rowCount - 1, 0) != "") {
                table.addRow(arrayOf("", ""))
            }
        }
        add(JPanel(FlowLayout(FlowLayout.LEFT)).apply { add(button) })
        return button
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

        var names = settings.labelCommands.keys

        for (i in 0 until table.rowCount) {
            val position = (table.getValueAt(i, 1) as String).toIntOrNull()
            if (position != null) {
                addOrUpdateStoredRow(table.getValueAt(i, 0) as String, position)
            }
            names.remove(table.getValueAt(i, 0) as String)
            //ToDo: add error message in case position isn't an integer
        }
        names.forEach{settings.labelCommands.remove(it)}
    }

    override fun reset() {
        automaticSoftWraps.isSelected = settings.automaticSoftWraps
        automaticSecondInlineMathSymbol.isSelected = settings.automaticSecondInlineMathSymbol
        automaticUpDownBracket.isSelected = settings.automaticUpDownBracket
        automaticItemInItemize.isSelected = settings.automaticItemInItemize
        var i = 0
        settings.labelCommands.forEach { command, position -> addOrUpdateRow(i++, command, position) }
    }

    private fun addOrUpdateStoredRow(command : String, position : Int) {
        if (settings.labelCommands.containsKey(command)) {
            settings.labelCommands[command] = position
        }
        else {
            settings.labelCommands.plus(Pair(command, position))
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
}
