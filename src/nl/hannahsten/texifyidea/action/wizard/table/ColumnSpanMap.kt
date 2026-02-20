package nl.hannahsten.texifyidea.action.wizard.table
/**
 * Implement this to define column spans in a table.
 *
 * Adapted from https://web.archive.org/web/20080726035429/http://www.swingwiki.org/howto:column_spanning
 */
open class ColumnSpanMap {
    /**
     * @param row logical cell row
     * @param column logical cell column
     * @return number of columns spanning the given cell
     */
    open fun numberOfColumnsInSpan(row: Int, column: Int): Int = 1
}

/**
 * @param row logical cell row
 * @param column logical cell column
 * @return the column index of the visible cell, for a span it would be the index of the left-most cell in the span.
 */
fun ColumnSpanMap.columnIndexOfVisibleCell(row: Int, column: Int): Int {
    for (j in 0..column) {
        val spanSize = numberOfColumnsInSpan(row, j)
        if (j + spanSize - 1 == column) {
            return j
        }
    }
    return column
}