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
 * Parse html to LaTeX using various converters.
 */
fun convertHtmlToLatex(nodes: List<Node>, latexFile: LatexFile): String {
    var out = ""

    for (node in nodes) {
        if (node.childNodeSize() == 0) {
            when (node) {
                is TextNode -> out += escapeText(node.text())
                is Element -> {
                    out += handleElement(node, latexFile)
                }
                else -> {
                    Log.error("Did not plan for " + node.javaClass.name + " please implement a case for this")
                }
            }
        }
        else {
            out += if (node is Element) {
                handleElement(node, latexFile)
            }
            else
                convertHtmlToLatex(node.childNodes(), latexFile)
        }
    }

    return out
}

/**
 * Convert one html element to LaTeX and append to the given StringBuilder.
 */
private fun handleElement(element: Element, psiFile: LatexFile): String {
    if (tagDependencies[element.tagName()] != null)
        (psiFile).insertUsepackage(tagDependencies[element.tagName()]!!)

    val htmlConverter = when (element.tagName()) {
        "table" -> TableHtmlToLatexConverter()
        "img" -> ImageHtmlToLatexConverter()
        else -> StyledTextHtmlToLatexConverter()
    }
    return htmlConverter.convertHtmlToLatex(element, psiFile)
}

private val tagDependencies = hashMapOf(
    "a" to LatexPackage.HYPERREF
)

fun htmlTextIsFormattable(htmlIn: String): Boolean =
    (PandocHtmlToLatexConverter.isPandocInPath && htmlIn.startsWith("<meta")) || openingTags.keys.any { htmlIn.contains("<$it>") } && closingTags.keys.any { htmlIn.contains("<$it>") }