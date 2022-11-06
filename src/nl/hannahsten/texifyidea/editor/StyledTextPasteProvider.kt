package nl.hannahsten.texifyidea.editor

import com.intellij.ide.PasteProvider
import com.intellij.openapi.actionSystem.DataContext
import com.intellij.openapi.actionSystem.PlatformDataKeys
import com.intellij.openapi.editor.actions.PasteAction
import nl.hannahsten.texifyidea.action.insert.InsertStyledText
import nl.hannahsten.texifyidea.util.Clipboard
import nl.hannahsten.texifyidea.util.Log
import nl.hannahsten.texifyidea.util.currentTextEditor
import nl.hannahsten.texifyidea.util.files.isLatexFile
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import org.jsoup.nodes.TextNode
import java.awt.datatransfer.DataFlavor
import java.util.*

/**
 * Pastes html and applies text(bf|it)
 *
 * @author jojo2357
 */
open class StyledTextPasteProvider : PasteProvider {

    val openingTags = hashMapOf(
        "i" to "\\textit{",
        "b" to "\\textbf{",
        "u" to "\\underline{",
        "ol" to "\\begin{enumerate}\n",
        "ul" to "\\begin{itemize}\n",
        "li" to "\\item ",
    )

    val closingTags = hashMapOf(
        "i" to "}",
        "b" to "}",
        "u" to "}",
        "ol" to "\\end{enumerate}\n",
        "ul" to "\\end{itemize}\n",
        "li" to "\n",
    )

    override fun performPaste(dataContext: DataContext) {
        val file = dataContext.getData(PlatformDataKeys.VIRTUAL_FILE) ?: return
        val project = dataContext.getData(PlatformDataKeys.PROJECT) ?: return
        val clipboardHtml = dataContext.transferableHtml() ?: return
        val html = Clipboard.extractHtmlFromClipboard(clipboardHtml)

        val tableTextToInsert = Jsoup.parse(html).parseText()

        val editor = dataContext.getData(PlatformDataKeys.PROJECT)?.currentTextEditor() ?: return

        InsertStyledText(tableTextToInsert).actionPerformed(file, project, editor)
    }

    override fun isPastePossible(dataContext: DataContext): Boolean {
        val file = dataContext.getData(PlatformDataKeys.PSI_FILE) ?: return false
        if (file.isLatexFile().not()) return false
        if (ShiftTracker.isShiftPressed()) return false

        val pasteData = dataContext.transferableHtml() ?: return false
        Log.warn("Attempting to paste $pasteData")
        return openingTags.keys.any { pasteData.contains("<$it>") } && closingTags.keys.any { pasteData.contains("<$it>") }
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

    private fun List<org.jsoup.nodes.Node>.parseToString(): String {
        val out = StringBuilder()

        for (node in this) {
            if (node.childNodeSize() == 0) {
                if (node is TextNode)
                    out.append(node.text())
                else if (node is Element) {
                    out.append(node.getPrefix()).append(node.text()).append(node.getPostfix())
                }
                else
                    throw IllegalStateException("Did not plan for " + node.javaClass.name + " please implement a case for this")
            }
            else {
                if (node is Element) {
                    out.append(node.getPrefix()).append(node.childNodes().parseToString()).append(node.getPostfix())
                }
                else
                    out.append(node.childNodes().parseToString())
            }
        }

        return out.toString()
    }

    /**
     * Creates the Table Creation Dialog filled in with the data from the clipboard.
     */
    private fun Document.parseText(): String {
        return select("body")[0].childNodes().parseToString()
    }

    private fun Element.getPrefix(): String {
        return openingTags[tagName()] ?: ""
    }

    private fun Element.getPostfix(): String {
        return closingTags[tagName()] ?: ""
    }
}