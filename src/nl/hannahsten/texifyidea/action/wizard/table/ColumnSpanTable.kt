package nl.hannahsten.texifyidea.action.wizard.table

import com.intellij.ui.table.JBTable
import java.awt.Point
import java.awt.Rectangle
import javax.swing.table.TableModel

/**
 * Table with a column span UI.
 *
 * Adapted from https://web.archive.org/web/20080726035429/http://www.swingwiki.org/howto:column_spanning
 */
class ColumnSpanTable(var map: ColumnSpanMap?, tbl: TableModel?) : JBTable(tbl) {
    init {
        setUI(ColumnSpanTableUI())
    }

    override fun getCellRect(row: Int, column: Int, includeSpacing: Boolean): Rectangle {
        // required because getCellRect is used in JTable constructor
        if (map == null) return super.getCellRect(row, column, includeSpacing)
        // add widths of all spanned logical cells
        val sk = map!!.columnIndexOfVisibleCell(row, column)
        val r1 = super.getCellRect(row, sk, includeSpacing)
        if (map!!.numberOfColumnsInSpan(row, sk) != 1) for (i in 1..<map!!.numberOfColumnsInSpan(row, sk)) {
            r1.width += getColumnModel().getColumn(sk + i).getWidth()
        }
        return r1
    }

    override fun columnAtPoint(p: Point): Int {
        val x = super.columnAtPoint(p)
        // -1 is returned by columnAtPoint if the point is not in the table
        if (x < 0) return x
        val y = super.rowAtPoint(p)
        return map!!.columnIndexOfVisibleCell(y, x)
    }
}
