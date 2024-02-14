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
        return LatexTableWizardAction().getTableTextWithDialog(
            file.project,
            htmlIn.ownerDocument()?.toTableDialogWrapper(file) ?: return ""
        )
    }

    /**
     * Creates the Table Creation Dialog filled in with the data from the clipboard.
     */
    @Suppress("USELESS_CAST")
    private fun Document.toTableDialogWrapper(latexFile: LatexFile): TableCreationDialogWrapper? {
        val rows = select("table tr")
        val height = rows.size
        val width = rows.firstOrNull()?.select("td, th")?.size ?: 0

        if (height == 0 && width == 0) return null

        // Convert html to data vector Vector<Vector<Any?>> as required by DefaultTableModel
        val header = rows.firstOrNull()?.select("td, th")?.mapNotNull { it.text() }?.toVector() ?: return null
        val content: Vector<Vector<Any?>> = rows.drop(1).map { tr ->
            tr.select("td, th").map { td -> convertHtmlToLatex(listOf(td), latexFile) as Any? }.toVector()
        }.toVector()

        // Find the type of column automatically.
        val contentRows = rows.drop(1)
        val columnTypes = (0 until width).map { col ->
            // Check if all contents of the column (except the header) can be converted to a number.
            // When that's the case => it's a number column. All other cases, text. Ignoring the Math option
            // as the table information is most probably something outside of a latex context.
            if (contentRows.all { it.select("td, th").getOrNull(col)?.text()?.toDoubleOrNull() != null }) {
                ColumnType.NUMBERS_COLUMN
            }
            else ColumnType.TEXT_COLUMN
        }

        return TableCreationDialogWrapper(
            columnTypes,
            TableCreationTableModel(content, header)
        )
    }
}