package nl.hannahsten.texifyidea.editor.pasteproviders

import nl.hannahsten.texifyidea.action.wizard.table.*
import nl.hannahsten.texifyidea.file.LatexFile
import nl.hannahsten.texifyidea.util.toVector
import org.jsoup.nodes.Element
import java.util.*

/**
 * Convert HTML tables to LaTeX using the [TableCreationDialogWrapper].
 */
class TableHtmlToLatexConverter : HtmlToLatexConverter {

    override fun convertHtmlToLatex(htmlIn: Element, file: LatexFile): String {
        return LatexTableWizardAction().getTableTextWithDialog(
            file.project,
            htmlIn.ownerDocument()?.toTableDialogWrapper(file) ?: return ""
        )
    }

    /**
     * Creates the Table Creation Dialog filled in with the data from the clipboard.
     */
    @Suppress("USELESS_CAST")
    private fun Element.toTableDialogWrapper(latexFile: LatexFile): TableCreationDialogWrapper? {
        // Work with this table element directly
        val table = this

        data class HtmlCell(val element: Element?, val text: String)

        // Expand table into a full grid accounting for rowspan/colspan
        val trs = table.select("tr")
        val assignments = mutableMapOf<Pair<Int, Int>, HtmlCell>()
        // Map grid locations to colspan size, if larger than 1
        val colspans = mutableMapOf<Pair<Int, Int>, Int>()
        var maxRowIndex = -1
        var maxColIndex = -1

        val header = mutableListOf<String>()
        trs.forEachIndexed { r, tr ->
            var c = 0
            val cells = tr.select("td, th")
            cells.forEach { td ->
                // Find next free column in this row (skip positions filled by previous rowspans)
                while (assignments.containsKey(Pair(r, c))) c++

                val rs = td.attr("rowspan").toIntOrNull()?.coerceAtLeast(1) ?: 1
                val cs = td.attr("colspan").toIntOrNull()?.coerceAtLeast(1) ?: 1
                // We don't support colspans in the header for now
                if (r > 0 && cs > 1) {
                    // - 1 because we don't count the header row for the UI
                    colspans[Pair(r - 1, c)] = cs
                }
                val cell = HtmlCell(td, td.text())

                for (dr in 0 until rs) {
                    for (dc in 0 until cs) {
                        val rr = r + dr
                        val cc = c + dc
                        assignments[Pair(rr, cc)] = cell
                        if (rr > maxRowIndex) maxRowIndex = rr
                        if (cc > maxColIndex) maxColIndex = cc

                        if (r == 0) {
                            // First row: collect header names
                            if (dc == 0) {
                                header.add(td.text())
                            }
                            else {
                                header.add("") // placeholder for spanned columns
                            }
                        }
                    }
                }
                c += cs
            }
        }

        val height = maxRowIndex + 1
        val width = maxColIndex + 1
        if (height <= 0 || width <= 0) return null

        val grid: List<List<HtmlCell?>> = (0 until height).map { r ->
            (0 until width).map { c -> assignments[Pair(r, c)] }
        }

        // Build content rows from the remaining grid rows
        val content: Vector<Vector<Any?>> = (1 until height).map { r ->
            (0 until width).map { c ->
                val cell = grid[r][c]
                if (cell?.element != null) {
                    convertHtmlToLatex(listOf(cell.element), latexFile) as Any?
                }
                else {
                    (cell?.text ?: "") as Any?
                }
            }.toVector()
        }.toVector()

        // Determine column types based on expanded content
        val columnTypes = (0 until width).map { col ->
            val allNumeric = (1 until height).all { r ->
                val t = grid[r][col]?.text
                t.isNullOrBlank() || t.trim().toDoubleOrNull() != null
            }
            if (allNumeric) ColumnType.NUMBERS_COLUMN else ColumnType.TEXT_COLUMN
        }

        val columnSpanMap = object : ColumnSpanMap() {
            override fun numberOfColumnsInSpan(row: Int, column: Int): Int = colspans[Pair(row, column)] ?: 1
        }

        return TableCreationDialogWrapper(
            columnTypes,
            TableCreationTableModel(content, header.toVector()),
            columnSpanMap,
        )
    }
}