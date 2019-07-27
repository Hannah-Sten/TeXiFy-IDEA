package nl.hannahsten.texifyidea.ui.tablecreationdialog

import java.util.*
import javax.swing.table.DefaultTableModel

/**
 * @author Abby Berkers
 */
class TableCreationTableModel : DefaultTableModel() {
    /**
     * Remove a column and its header from the table.
     */
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

    /**
     * Set the name of a header.
     */
    fun setHeaderName(name: String, column: Int) {
        columnIdentifiers[column] = name
    }

    /**
     * Adds an empty row to the table.
     */
    fun addEmptyRow() {
        val emptyRow = (0 until columnCount).map { "" }.toTypedArray()
        addRow(emptyRow)
    }

    /**
     * Get the names of the columns.
     */
    fun getColumnNames() = columnIdentifiers
}