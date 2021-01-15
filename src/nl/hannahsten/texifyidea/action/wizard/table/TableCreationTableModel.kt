package nl.hannahsten.texifyidea.action.wizard.table

import java.util.*
import javax.swing.table.DefaultTableModel

/**
 * @author Abby Berkers
 */
class TableCreationTableModel : DefaultTableModel {

    constructor(data: Vector<Vector<Any?>>, columnNames: Vector<String>) : super(data, columnNames)

    constructor() : this(Vector(), Vector())

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
        val emptyRow = Array(columnCount) { "" }
        addRow(emptyRow)
    }

    /**
     * Get the names of the columns.
     */
    fun getColumnNames(): Vector<Any> = columnIdentifiers
}