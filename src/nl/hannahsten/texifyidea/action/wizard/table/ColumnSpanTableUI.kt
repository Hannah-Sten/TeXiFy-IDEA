package nl.hannahsten.texifyidea.action.wizard.table

import java.awt.Graphics
import java.awt.Point
import java.awt.Rectangle
import javax.swing.JComponent
import javax.swing.plaf.basic.BasicTableUI

/**
 * TableUI supporting column spans.
 *
 * Adapted from https://web.archive.org/web/20080726035429/http://www.swingwiki.org/howto:column_spanning
 */
class ColumnSpanTableUI : BasicTableUI() {
    override fun paint(g: Graphics, c: JComponent?) {
        val r = g.clipBounds
        val firstRow = table.rowAtPoint(Point(0, r.y))
        var lastRow = table.rowAtPoint(Point(0, r.y + r.height))
        // -1 is a flag that the ending point is outside the table
        if (lastRow < 0) lastRow = table.rowCount - 1
        for (i in firstRow..lastRow) paintRow(i, g)
    }

    private fun paintRow(row: Int, g: Graphics) {
        val r = g.clipBounds
        var i = 0
        while (i < table.columnCount) {
            val r1 = table.getCellRect(row, i, true)
            if (r1.intersects(r)) // at least a part is visible
                {
                    val sk = (table as ColumnSpanTable).map!!.columnIndexOfVisibleCell(row, i)
                    paintCell(row, sk, g, r1)
                    // increment the column counter
                    i += (table as ColumnSpanTable).map!!.numberOfColumnsInSpan(row, sk) - 1
                }
            i++
        }
    }

    private fun paintCell(row: Int, column: Int, g: Graphics, area: Rectangle) {
        val verticalMargin = table.getRowMargin()
        val horizontalMargin = table.getColumnModel().columnMargin

        val c = g.color
        g.color = table.getGridColor()
        g.drawRect(area.x, area.y, area.width - 1, area.height - 1)
        g.color = c

        area.setBounds(
            area.x + horizontalMargin / 2,
            area.y + verticalMargin / 2,
            area.width - horizontalMargin,
            area.height - verticalMargin
        )

        if (table.isEditing && table.getEditingRow() == row && table.getEditingColumn() == column) {
            val component = table.editorComponent
            component.bounds = area
            component.validate()
        } else {
            val renderer = table.getCellRenderer(row, column)
            val component = table.prepareRenderer(renderer, row, column)
            if (component.getParent() == null) rendererPane.add(component)
            rendererPane.paintComponent(
                g, component, table, area.x, area.y,
                area.width, area.height, true
            )
        }
    }
}