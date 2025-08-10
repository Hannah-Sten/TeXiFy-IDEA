package nl.hannahsten.texifyidea.editor.pasteproviders

import nl.hannahsten.texifyidea.action.wizard.table.ColumnType
import nl.hannahsten.texifyidea.action.wizard.table.LatexTableWizardAction
import nl.hannahsten.texifyidea.action.wizard.table.TableCreationDialogWrapper
import nl.hannahsten.texifyidea.action.wizard.table.TableCreationTableModel
import nl.hannahsten.texifyidea.file.LatexFile
import nl.hannahsten.texifyidea.lang.LatexPackage
import nl.hannahsten.texifyidea.util.insertUsepackage
import nl.hannahsten.texifyidea.util.toVector
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import java.util.*

/**
 * Convert HTML tables to LaTeX using the [TableCreationDialogWrapper].
 */
class TableHtmlToLatexConverter : HtmlToLatexConverter {

    override fun convertHtmlToLatex(htmlIn: Element, file: LatexFile): String {
        // If this is a table, try to generate LaTeX with merged headers first; otherwise fall back to the wizard
        if (htmlIn.tagName() == "table") {
            generateLatexWithMergedHeaders(htmlIn, file)?.let { return it }
        }
        // Fall back to the wizard dialog (single header row support)
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

        // Build single-row header: prefer the most granular (bottom) header label per column, avoid concatenating with separators
        val header = (0 until width).map { col ->
            val labels = mutableListOf<String>()
            for (r in 0 until headerRowCount) {
                val t = grid[r][col]?.text?.trim().orEmpty()
                if (t.isNotEmpty() && (labels.isEmpty() || labels.last() != t)) {
                    labels.add(t)
                }
            }
            // Use the last non-empty label (closest to data rows). If none, fall back to a generic name.
            labels.lastOrNull() ?: "Column ${col + 1}"
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

    /**
     * Try to generate LaTeX directly with merged headers (using \multicolumn) when the table contains
     * multiple header rows or colspan attributes. Returns null if we should fall back to the wizard.
     */
    private fun generateLatexWithMergedHeaders(table: Element, latexFile: LatexFile): String? {
        // Build the expanded grid (same approach as in toTableDialogWrapper)
        data class HtmlCell(val element: Element?, val text: String)
        val trs = table.select("tr")
        if (trs.isEmpty()) return null
        val assignments = mutableMapOf<Pair<Int, Int>, HtmlCell>()
        var maxRowIndex = -1
        var maxColIndex = -1
        var anyColOrRowSpan = false

        trs.forEachIndexed { r, tr ->
            var c = 0
            val cells = tr.select("td, th")
            cells.forEach { td ->
                while (assignments.containsKey(Pair(r, c))) c++
                val rs = td.attr("rowspan").toIntOrNull()?.coerceAtLeast(1) ?: 1
                val cs = td.attr("colspan").toIntOrNull()?.coerceAtLeast(1) ?: 1
                if (rs > 1 || cs > 1) anyColOrRowSpan = true
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

        // Heuristic for header rows: until a row is mostly numeric
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

        // Only generate directly if there are multiple header rows or explicit colspan/rowspan
        if (headerRowCount <= 1 && !anyColOrRowSpan) return null

        // Determine alignments from data rows
        val columnTypes = (0 until width).map { col ->
            val allNumeric = (headerRowCount until height).all { r ->
                val t = grid[r][col]?.text
                t == null || t.isBlank() || t.trim().toDoubleOrNull() != null
            }
            if (allNumeric) ColumnType.NUMBERS_COLUMN else ColumnType.TEXT_COLUMN
        }
        val colSpec = columnTypes.joinToString("") { if (it == ColumnType.NUMBERS_COLUMN) "r" else "l" }

        // Helper to convert an HtmlCell to LaTeX content (preserving styled text if present)
        fun HtmlCell?.toLatex(): String = when {
            this == null -> ""
            this.element != null -> convertHtmlToLatex(listOf(this.element), latexFile)
            else -> this.text
        }

        // Build header rows using multicolumn for repeated cells across the row
        // Build header rows using multicolumn for repeated cells across the row,
// and insert \cmidrule under non-leaf header rows (skip vertically merged cells).
        val headerRowsLatex = buildString {
            for (r in 0 until headerRowCount) {
                var c = 0
                val parts = mutableListOf<String>()
                val spans = mutableListOf<Pair<Int, Int>>() // 1-based [start, end] for cmidrule

                while (c < width) {
                    val cell = grid[r][c]
                    // Determine span width across this row for equal cell references
                    var span = 1
                    while (c + span < width && grid[r][c + span] === cell) span++

                    val content = cell.toLatex().trim()
                    val piece = if (span > 1) {
                        "\\multicolumn{$span}{c}{$content}"
                    } else content
                    parts.add(piece)

                    // Only add cmidrule if this cell is not vertically merged into next row
                    if (span > 1) {
                        val verticallyMerged =
                            (r + 1 < height) && (grid[r + 1][c] === cell)
                        if (!verticallyMerged) {
                            val start = c + 1 // LaTeX is 1-based
                            val end = c + span
                            spans.add(start to end)
                        }
                    }

                    c += span
                }

                // Output the current header row
                append(parts.joinToString(" & "))
                append(" \\\\\n")

                // Output cmidrule for this header row if not the last header row
                if (r < headerRowCount - 1 && spans.isNotEmpty()) {
                    val rules = spans.joinToString(" ") { (s, e) -> "\\cmidrule(lr){$s-$e}" }
                    append(rules).append("\n")
                }
            }
        }


        // Build data rows
        val dataRowsLatex = buildString {
            for (r in headerRowCount until height) {
                val row = (0 until width).joinToString(" & ") { grid[r][it].toLatex() }
                append(row).append(" \\\\\n")
            }
        }

        // Ensure booktabs is available
        latexFile.insertUsepackage(LatexPackage.BOOKTABS)

        // Compose full table
        val indent = ""
        val tabIndent = "    "
        return buildString {
            append("\\begin{table}\n")
            append("${indent}${tabIndent}\\centering\n")
            append("${indent}${tabIndent}\\begin{tabular}{").append(colSpec).append("}\n")
            append("${indent}${tabIndent}${tabIndent}\\toprule\n")
            append(headerRowsLatex)
            append("${indent}${tabIndent}${tabIndent}\\midrule\n")
            append(dataRowsLatex)
            append("${indent}${tabIndent}${tabIndent}\\bottomrule\n")
            append("${indent}${tabIndent}\\end{tabular}\n")
            append("${indent}${tabIndent}\\caption{}\n")
            append("${indent}${tabIndent}\\label{tab:}\n")
            append("${indent}\\end{table}\n")
        }
    }
}