package nl.rubensten.texifyidea.settings.labeldefiningcommands

import com.intellij.util.ui.ItemRemovable
import javax.swing.table.DefaultTableModel

/**
 * tableModel to prevent editing the cell itself so the user is forced to use the edit window
 */
class LabelCommandSettingsTableModel : DefaultTableModel(), ItemRemovable {
    override fun isCellEditable(row: Int, column: Int): Boolean {
        return false
    }
}
