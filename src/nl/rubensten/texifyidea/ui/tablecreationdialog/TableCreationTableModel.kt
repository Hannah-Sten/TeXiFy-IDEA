package nl.rubensten.texifyidea.ui.tablecreationdialog

import java.util.*
import javax.swing.table.DefaultTableModel

class TableCreationTableModel : DefaultTableModel() {
    fun removeColumn(column: Int) {
        // Remove the column from the data.
        dataVector.forEach {
            (it as Vector<*>).removeAt(column)
        }

        // Remove the header.
        columnIdentifiers.removeAt(column)

        // Let the table now that its structure has changed.
        fireTableStructureChanged()
    }
}