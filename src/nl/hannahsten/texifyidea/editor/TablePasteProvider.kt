package nl.hannahsten.texifyidea.editor

import com.intellij.ide.PasteProvider
import com.intellij.openapi.actionSystem.DataContext
import com.intellij.openapi.actionSystem.PlatformDataKeys
import com.intellij.openapi.editor.actions.PasteAction
import nl.hannahsten.texifyidea.action.wizard.table.ColumnType
import nl.hannahsten.texifyidea.action.wizard.table.LatexTableWizardAction
import nl.hannahsten.texifyidea.action.wizard.table.TableCreationDialogWrapper
import nl.hannahsten.texifyidea.action.wizard.table.TableCreationTableModel
import nl.hannahsten.texifyidea.util.Clipboard
import nl.hannahsten.texifyidea.util.files.isLatexFile
import nl.hannahsten.texifyidea.util.toVector
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import java.awt.datatransfer.DataFlavor
import java.util.*

/**
 * Pastes the table html into a Insert Table Wizard if applicable.
 *
 * @author Hannah Schellekens
 */
open class TablePasteProvider : PasteProvider {

    override fun performPaste(dataContext: DataContext) {
        val file = dataContext.getData(PlatformDataKeys.VIRTUAL_FILE) ?: return
        val project = dataContext.getData(PlatformDataKeys.PROJECT) ?: return
        val clipboardHtml = dataContext.transferableHtml() ?: return
        val html = Clipboard.extractHtmlFromClipboard(clipboardHtml) ?: return
        val tableDialog = Jsoup.parse(html).toTableDialogWrapper() ?: return

        LatexTableWizardAction().executeAction(file, project, tableDialog)
    }

    override fun isPastePossible(dataContext: DataContext): Boolean {
        return false
        val file = dataContext.getData(PlatformDataKeys.PSI_FILE) ?: return false
        if (file.isLatexFile().not()) return false
        if (ShiftTracker.isShiftPressed()) return false

        val pasteData = dataContext.transferableHtml() ?: return false
        return pasteData.contains("<table", ignoreCase = true)
    }

    override fun isPasteEnabled(dataContext: DataContext) = isPastePossible(dataContext)

    /**
     * Extracts the HTML on clipboard if there is HTML on the clipboard.
     */
    private fun DataContext.transferableHtml(): String? {
        val pasteData = getData(PasteAction.TRANSFERABLE_PROVIDER)?.produce() ?: return null
        return if (pasteData.isDataFlavorSupported(DataFlavor.allHtmlFlavor)) {
            pasteData.getTransferData(DataFlavor.allHtmlFlavor) as String
        }
        else null
    }

    /**
     * Creates the Table Creation Dialog filled in with the data from the clipboard.
     */
    @Suppress("USELESS_CAST")
    private fun Document.toTableDialogWrapper(): TableCreationDialogWrapper? {
        val rows = select("table tr")
        val height = rows.size
        val width = rows.firstOrNull()?.select("td, th")?.size ?: 0

        if (height == 0 && width == 0) return null

        // Convert html to data vector Vector<Vector<Any?>> and headers.
        val header = rows.firstOrNull()?.select("td, th")?.mapNotNull { it.text() }?.toVector() ?: return null
        val content: Vector<Vector<Any?>> = rows.drop(1).map { tr ->
            tr.select("td, th").map { td -> td.handleHtmlFormatting() as Any? }.toVector()
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

    /**
     * Converts <b>/<i>/<u> tags to latex formatting commands.
     * `this` is a <td> HTML Element.
     */
    private fun Element.handleHtmlFormatting(): String {
        val prefix = StringBuilder()
        val suffix = StringBuilder()

        if (select("b, strong").isNotEmpty()) {
            prefix.append("\\textbf{")
            suffix.append("}")
        }
        if (select("i, em").isNotEmpty()) {
            prefix.append("\\textit{")
            suffix.append("}")
        }
        if (select("u").isNotEmpty()) {
            prefix.append("\\underline{")
            suffix.append("}")
        }

        return prefix
            .append(text())
            .append(suffix.toString())
            .toString()
    }
}