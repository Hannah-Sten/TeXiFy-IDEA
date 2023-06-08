package nl.hannahsten.texifyidea.editor.pasteproviders

import com.intellij.openapi.actionSystem.DataContext
import com.intellij.openapi.actionSystem.PlatformDataKeys
import com.intellij.openapi.project.Project
import nl.hannahsten.texifyidea.file.LatexFile
import nl.hannahsten.texifyidea.lang.LatexPackage
import nl.hannahsten.texifyidea.util.Log
import nl.hannahsten.texifyidea.util.insertUsepackage
import org.jsoup.nodes.Element
import org.jsoup.nodes.Node
import org.jsoup.nodes.TextNode

/**
 * Paste providers which can handle certain html tags.
 */
private val childHandlers = hashMapOf(
    "table" to TablePasteProvider(),
    "img" to ClipboardHtmlImagePasteProvider(),
)

/**
 * Map HTML tags to LaTeX
 */
private val openingTags = hashMapOf(
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

private val closingTags = hashMapOf(
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

private val tagDependencies = hashMapOf<String, LatexPackage>(
    "a" to LatexPackage.HYPERREF
)

private val specialOpeningTags = hashMapOf<String, (Element) -> String>(
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

private val specialClosingTags = hashMapOf<String, (Element) -> String>(
    "a" to { element ->
        if (element.hasAttr("href"))
            "}"
        else if (element.hasAttr("name"))
            "}"
        else
            ""
    }
)

private val escapeChars = hashMapOf(
    "%" to "\\%",
    "&" to "\\&",
    "_" to "\\_",
    "#" to "\\#",
    "$" to "\\$",
    "{" to "\\{",
    "}" to "\\}",
    "^" to "\\^",
    "~" to "\\~",
    "−" to "-"
)

/**
 * Parse given HTML nodes to LaTeX using direct hardcoded mappings.
 */
fun parseToString(nodes: List<Node>, project: Project, dataContext: DataContext): String {
    val out = StringBuilder()

    for (node in nodes) {
        if (node.childNodeSize() == 0) {
            when (node) {
                is TextNode -> out.append(escapeText(node.text()))
                is Element -> {
                    handleElement(node, out, project, dataContext)
                }

                else -> throw IllegalStateException("Did not plan for " + node.javaClass.name + " please implement a case for this")
            }
        }
        else {
            if (node is Element) {
                handleElement(node, out, project, dataContext)
            }
            else
                out.append(parseToString(node.childNodes(), project, dataContext))
        }
    }

    return out.toString()
}

/**
 * Convert one html element to LaTeX and append to the given StringBuilder.
 */
private fun handleElement(element: Element, out: StringBuilder, project: Project, dataContext: DataContext) {
    if (tagDependencies[element.tagName()] != null)
        (dataContext.getData(PlatformDataKeys.PSI_FILE) as? LatexFile)?.insertUsepackage(tagDependencies[element.tagName()]!!)

    if (hasSpecialHandler(element)) {
        // todo move childHandlers to responsibility of paste providers (isPastePossible)
        out.append(childHandlers[element.tagName()]?.convertHtmlToLatex(element, dataContext))
    }
    else {
        val environ = getCentering(element)
        if (environ != "") {
            out.append("\\begin{$environ}")
        }
        out.append(getPrefix(element))

        if (element.childNodeSize() > 0)
            out.append(parseToString(element.childNodes(), project, dataContext))
        else
            out.append(escapeText(element.text()))

        out.append(getPostfix(element))
        if (environ != "") {
            out.append("\\end{$environ}")
        }
    }
}

private fun getCentering(node: Node) = when {
    node.attr("align") == "center" -> "center"
    node.attr("align") == "left" -> "flushleft"
    node.attr("align") == "right" -> "flushright"
    // latex doesnt have native support here
    // node.attr("align") == "justify" ->
    else -> ""
}

private fun hasSpecialHandler(element: Element): Boolean {
    return childHandlers.keys.contains(element.tagName())
}

private fun getPrefix(element: Element): String {
    if (specialOpeningTags[element.tagName()] == null && openingTags[element.tagName()] == null)
        Log.warn("Couldn't find a home for " + element.tagName())
    return specialOpeningTags[element.tagName()]?.invoke(element) ?: openingTags[element.tagName()] ?: ""
}

private fun getPostfix(element: Element): String {
    return specialClosingTags[element.tagName()]?.invoke(element) ?: closingTags[element.tagName()] ?: ""
}

private fun escapeText(stringin: String): String {
    var out = stringin.replace("\\", "\\textbackslash ")

    escapeChars.forEach { out = out.replace(it.key, it.value) }

    return out
}

fun htmlTextIsFormattable(htmlIn: String): Boolean =
    (PandocPasteProvider.isPandocInPath && htmlIn.startsWith("<meta")) || openingTags.keys.any { htmlIn.contains("<$it>") } && closingTags.keys.any { htmlIn.contains("<$it>") }