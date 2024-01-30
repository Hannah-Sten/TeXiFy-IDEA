package nl.hannahsten.texifyidea.editor.pasteproviders

import nl.hannahsten.texifyidea.editor.pasteproviders.StyledTextHtmlToLatexConverter.Companion.closingTags
import nl.hannahsten.texifyidea.editor.pasteproviders.StyledTextHtmlToLatexConverter.Companion.escapeText
import nl.hannahsten.texifyidea.editor.pasteproviders.StyledTextHtmlToLatexConverter.Companion.openingTags
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
    "table" to TableHtmlToLatexConverter(),
    "img" to ImageHtmlToLatexConverter(),
)


/**
 * Parse given HTML nodes to LaTeX using direct hardcoded mappings.
 */
fun parseToString(nodes: List<Node>, latexFile: LatexFile): String {
    val out = StringBuilder()

    for (node in nodes) {
        if (node.childNodeSize() == 0) {
            when (node) {
                is TextNode -> out.append(escapeText(node.text()))
                is Element -> {
                    handleElement(node, out, latexFile)
                }
                else -> {
                    Log.error("Did not plan for " + node.javaClass.name + " please implement a case for this")
                }
            }
        }
        else {
            if (node is Element) {
                handleElement(node, out, latexFile)
            }
            else
                out.append(parseToString(node.childNodes(), latexFile))
        }
    }

    return out.toString()
}

/**
 * Convert one html element to LaTeX and append to the given StringBuilder.
 * todo work out the dataContext everywhere
 */
private fun handleElement(element: Element, out: StringBuilder, psiFile: LatexFile) {
    if (tagDependencies[element.tagName()] != null)
        (psiFile).insertUsepackage(tagDependencies[element.tagName()]!!)

    if (hasSpecialHandler(element)) {
        // todo simply use if/else checks to defer to the right handler
        out.append(childHandlers[element.tagName()]?.convertHtmlToLatex(element, psiFile))
    }
    else {
        out.append(StyledTextHtmlToLatexConverter().convertHtmlToLatex(element, psiFile))
    }
}

private val tagDependencies = hashMapOf(
    "a" to LatexPackage.HYPERREF
)

private fun hasSpecialHandler(element: Element): Boolean {
    return childHandlers.keys.contains(element.tagName())
}

fun htmlTextIsFormattable(htmlIn: String): Boolean =
    (PandocHtmlToLatexConverter.isPandocInPath && htmlIn.startsWith("<meta")) || openingTags.keys.any { htmlIn.contains("<$it>") } && closingTags.keys.any { htmlIn.contains("<$it>") }