package nl.rubensten.texifyidea.settings

import com.intellij.util.ui.ItemRemovable
import javax.swing.table.DefaultTableModel

class MyTableModel : DefaultTableModel(), ItemRemovable {
    override fun isCellEditable(row: Int, column: Int): Boolean {
        return false
    }
}
