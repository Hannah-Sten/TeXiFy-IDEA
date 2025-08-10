package nl.hannahsten.texifyidea.editor.pasteproviders

import nl.hannahsten.texifyidea.action.wizard.table.ColumnType
import nl.hannahsten.texifyidea.action.wizard.table.LatexTableWizardAction
import nl.hannahsten.texifyidea.action.wizard.table.TableCreationDialogWrapper
import nl.hannahsten.texifyidea.action.wizard.table.TableCreationTableModel
import nl.hannahsten.texifyidea.file.LatexFile
import nl.hannahsten.texifyidea.util.toVector
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import java.util.*

/**
 * Convert HTML tables to LaTeX using the [TableCreationDialogWrapper].
 */
class TableHtmlToLatexConverter : HtmlToLatexConverter {

    override fun convertHtmlToLatex(htmlIn: Element, file: LatexFile): String {
        // htmlIn is expected to be the <table> element; use it directly rather than the document's first table
        val tableWrapper = if (htmlIn.tagName() == "table") htmlIn.toTableDialogWrapper(file) else null
        return LatexTableWizardAction().getTableTextWithDialog(
            file.project,
            tableWrapper ?: return ""
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
        var maxRowIndex = -1
        var maxColIndex = -1

        trs.forEachIndexed { r, tr ->
            var c = 0
            val cells = tr.select("td, th")
            cells.forEach { td ->
                // Find next free column in this row (skip positions filled by previous rowspans)
                while (assignments.containsKey(Pair(r, c))) c++

                val rs = td.attr("rowspan").toIntOrNull()?.coerceAtLeast(1) ?: 1
                val cs = td.attr("colspan").toIntOrNull()?.coerceAtLeast(1) ?: 1
                val cell = HtmlCell(td, td.text())

                for (dr in 0 until rs) {
                    for (dc in 0 until cs) {
                        val rr = r + dr
                        val cc = c + dc
                        assignments[Pair(rr, cc)] = cell
                        if (rr > maxRowIndex) maxRowIndex = rr
                        if (cc > maxColIndex) maxColIndex = cc
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

        fun isNumeric(s: String?): Boolean = s?.trim()?.toDoubleOrNull() != null

        // Heuristic to detect how many top rows are header rows:
        // Count from top until a row looks "data-like" (majority numeric). Fallback to 1 header row.
        val headerRowCount = run {
            var count = 0
            for (r in 0 until height) {
                val values = grid[r].map { it?.text ?: "" }
                val nonEmpty = values.count { it.isNotBlank() }
                val numeric = values.count { isNumeric(it) }
                val isData = nonEmpty > 0 && numeric >= (width + 1) / 2
                if (isData) break
                count++
            }
            if (count == 0) 1 else count
        }.coerceAtMost(height)

        // Build single-row header by stacking labels from header rows for each column
        val header = (0 until width).map { col ->
            val labels = mutableListOf<String>()
            for (r in 0 until headerRowCount) {
                val t = grid[r][col]?.text?.trim().orEmpty()
                if (t.isNotEmpty() && (labels.isEmpty() || labels.last() != t)) {
                    labels.add(t)
                }
            }
            if (labels.isNotEmpty()) labels.joinToString(" / ") else "Column ${col + 1}"
        }.toVector()

        // Build content rows from the remaining grid rows
        val content: Vector<Vector<Any?>> = (headerRowCount until height).map { r ->
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
            val allNumeric = (headerRowCount until height).all { r ->
                val t = grid[r][col]?.text
                t == null || t.isBlank() || t.trim().toDoubleOrNull() != null
            }
            if (allNumeric) ColumnType.NUMBERS_COLUMN else ColumnType.TEXT_COLUMN
        }

        return TableCreationDialogWrapper(
            columnTypes,
            TableCreationTableModel(content, header)
        )
    }
}