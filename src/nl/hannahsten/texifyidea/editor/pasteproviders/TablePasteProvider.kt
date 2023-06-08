package nl.hannahsten.texifyidea.editor.pasteproviders

import com.intellij.openapi.actionSystem.DataContext
import com.intellij.openapi.actionSystem.PlatformDataKeys
import com.intellij.openapi.project.Project
import nl.hannahsten.texifyidea.action.wizard.table.ColumnType
import nl.hannahsten.texifyidea.action.wizard.table.LatexTableWizardAction
import nl.hannahsten.texifyidea.action.wizard.table.TableCreationDialogWrapper
import nl.hannahsten.texifyidea.action.wizard.table.TableCreationTableModel
import nl.hannahsten.texifyidea.util.toVector
import org.jsoup.nodes.Document
import org.jsoup.nodes.Node
import java.util.*

class TablePasteProvider : LatexPasteProvider {

    override fun convertHtmlToLatex(htmlIn: Node, dataContext: DataContext): String {
        val project = dataContext.getData(PlatformDataKeys.PROJECT) ?: return ""
        return LatexTableWizardAction().executeAction(
            dataContext.getData(PlatformDataKeys.PROJECT) ?: return "",
            htmlIn.ownerDocument()?.toTableDialogWrapper(
                project,
                dataContext
            ) ?: return ""
        )
    }

    /**
     * Creates the Table Creation Dialog filled in with the data from the clipboard.
     */
    @Suppress("USELESS_CAST")
    private fun Document.toTableDialogWrapper(project: Project, dataContext: DataContext): TableCreationDialogWrapper? {
        val rows = select("table tr")
        val height = rows.size
        val width = rows.firstOrNull()?.select("td, th")?.size ?: 0

        if (height == 0 && width == 0) return null

        // Convert html to data vector Vector<Vector<Any?>> and headers.
        val header = rows.firstOrNull()?.select("td, th")?.mapNotNull { it.text() }?.toVector() ?: return null
        val content: Vector<Vector<Any?>> = rows.drop(1).map { tr ->
            tr.select("td, th").map { td -> parseToString(td.children(), project, dataContext) as Any? }.toVector()
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