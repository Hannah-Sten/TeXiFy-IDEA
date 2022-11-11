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

/**
 * Pastes html and applies text(bf|it)
 *
 * @author jojo2357
 */
class StyledTextPasteProvider : PasteProvider {

    val openingTags = hashMapOf(
        "i" to "\\textit{",
        "b" to "\\textbf{",
        "u" to "\\underline{",
        "p" to "",
        "ol" to "\\begin{enumerate}\n",
        "ul" to "\\begin{itemize}\n",
        "li" to "\\item ",
        "br" to "\n",
        "sup" to "\\textsuperscript{",
        "sub" to "\\textsubscript{",
        "h1" to "\\chapter*{",
        "h2" to "\\section*{",
        "h3" to "\\subsection*{",
        "h4" to "\\subsubsection*{",
        "h5" to "\\subsubsubsection*{",
    )

    val closingTags = hashMapOf(
        "i" to "}",
        "b" to "}",
        "u" to "}",
        "p" to "\n\\par\n",
        "ol" to "\\end{enumerate}\n",
        "ul" to "\\end{itemize}\n",
        "li" to "\n",
        "a" to "}",
        "br" to "",
        "sup" to "}",
        "sub" to "}",
        "h1" to "}\n",
        "h2" to "}\n",
        "h3" to "}\n",
        "h4" to "}\n",
        "h5" to "}\n",
    )

    val specialOpeningTags = hashMapOf<String, (Element) -> String>(
        "a" to { element ->
            if (element.hasAttr("href"))
                if (element.attr("href").startsWith("#"))
                    "\\hyperlink{" + element.attr("href").replace(Regex("^#"), "") + "}{"
                else
                    "\\href{" + escapeText(element.attr("href")) + "}{"
            else if (element.hasAttr("name"))
                "\\hypertarget{" + element.attr("name") + "}{"
            else
                ""
        }
    )

    val specialClosingTags = hashMapOf<String, (Element) -> String>(
        "a" to { element ->
            if (element.hasAttr("href"))
                "}"
            else if (element.hasAttr("name"))
                "}"
            else
                ""
        }
    )

    val escapeChars = hashMapOf(
        "%" to "\\%",
        "&" to "\\&",
        "_" to "\\_",
        "#" to "\\#",
        "−" to "-"
    )

    override fun isPastePossible(dataContext: DataContext): Boolean {
        val file = dataContext.getData(PlatformDataKeys.PSI_FILE) ?: return false
        if (file.isLatexFile().not()) return false
        if (ShiftTracker.isShiftPressed()) return false

        val pasteData = dataContext.transferableHtml() ?: return false
        Log.warn("Attempting to paste $pasteData")
        return openingTags.keys.any { pasteData.contains("<$it>") } && closingTags.keys.any { pasteData.contains("<$it>") }
    }

    override fun performPaste(dataContext: DataContext) {
        val file = dataContext.getData(PlatformDataKeys.VIRTUAL_FILE) ?: return
        val project = dataContext.getData(PlatformDataKeys.PROJECT) ?: return
        val clipboardHtml = dataContext.transferableHtml() ?: return
        val html = Clipboard.extractHtmlFromClipboard(clipboardHtml)

        val tableTextToInsert = Jsoup.parse(html).parseText()

        val editor = dataContext.getData(PlatformDataKeys.PROJECT)?.currentTextEditor() ?: return

        InsertStyledText(tableTextToInsert).actionPerformed(file, project, editor)
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

    private fun escapeText(stringin: String): String {
        var out = stringin

        escapeChars.forEach { out = out.replace(it.key, it.value) }

        return out
    }

    private fun List<org.jsoup.nodes.Node>.parseToString(): String {
        val out = StringBuilder()

        for (node in this) {
            if (node.childNodeSize() == 0) {
                if (node is TextNode)
                    out.append(escapeText(node.text()))
                else if (node is Element) {
                    val environ =
                        when {
                            node.attr("align") == "center" -> "center"
                            node.attr("align") == "left" -> "flushleft"
                            node.attr("align") == "right" -> "flushright"
                            // latex doesnt have native support here
                            // node.attr("align") == "justify" ->
                            else -> ""
                        }
                    if (environ != "") {
                        out.append("\\begin{$environ}").append(node.getPrefix()).append(escapeText(node.text())).append(node.getPostfix()).append("\\end{$environ}")
                    }
                    else
                        out.append(node.getPrefix()).append(escapeText(node.text())).append(node.getPostfix())
                }
                else
                    throw IllegalStateException("Did not plan for " + node.javaClass.name + " please implement a case for this")
            }
            else {
                if (node is Element) {
                    val environ =
                        when {
                            node.attr("align") == "center" -> "center"
                            node.attr("align") == "left" -> "flushleft"
                            node.attr("align") == "right" -> "flushright"
                            // latex doesnt have native support here
                            // node.attr("align") == "justify" ->
                            else -> ""
                        }
                    if (environ != "") {
                        out.append("\\begin{$environ}").append(node.getPrefix()).append(node.childNodes().parseToString()).append(node.getPostfix()).append("\\end{$environ}")
                    }
                    else
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
        if (specialOpeningTags[tagName()] == null && openingTags[tagName()] == null)
            Log.warn("Couldnt find a home for " + tagName())
        return specialOpeningTags[tagName()]?.invoke(this) ?: openingTags[tagName()] ?: ""
    }

    private fun Element.getPostfix(): String {
        return specialClosingTags[tagName()]?.invoke(this) ?: closingTags[tagName()] ?: ""
    }
}