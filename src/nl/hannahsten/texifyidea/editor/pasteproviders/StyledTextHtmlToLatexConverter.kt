package nl.hannahsten.texifyidea.editor.pasteproviders

import nl.hannahsten.texifyidea.file.LatexFile
import nl.hannahsten.texifyidea.util.Log
import org.jsoup.nodes.Element
import org.jsoup.nodes.Node

class StyledTextHtmlToLatexConverter : HtmlToLatexConverter {

    companion object {

        /**
         * Map HTML tags to LaTeX
         */
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

        val escapeChars = hashMapOf(
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


        fun escapeText(stringin: String): String {
            var out = stringin.replace("\\", "\\textbackslash ")

            escapeChars.forEach { out = out.replace(it.key, it.value) }

            return out
        }
    }


    override fun convertHtmlToLatex(htmlIn: Element, file: LatexFile): String {
        var latexString = ""
        val environ = getCentering(htmlIn)
        if (environ != "") {
            latexString += "\\begin{$environ}"
        }
        latexString += getPrefix(htmlIn)

        if (htmlIn.childNodeSize() > 0)
            latexString += parseToString(htmlIn.childNodes(), file)
        else
            latexString += escapeText(htmlIn.text())

        latexString += getPostfix(htmlIn)
        if (environ != "") {
            latexString += "\\end{$environ}"
        }
        return latexString

    }


    private fun getCentering(node: Node) = when {
        node.attr("align") == "center" -> "center"
        node.attr("align") == "left" -> "flushleft"
        node.attr("align") == "right" -> "flushright"
        // latex doesnt have native support here
        // node.attr("align") == "justify" ->
        else -> ""
    }


    private fun getPrefix(element: Element): String {
        if (specialOpeningTags[element.tagName()] == null && openingTags[element.tagName()] == null)
            Log.warn("Couldn't find a home for " + element.tagName())
        return specialOpeningTags[element.tagName()]?.invoke(element) ?: openingTags[element.tagName()] ?: ""
    }

    private fun getPostfix(element: Element): String {
        return specialClosingTags[element.tagName()]?.invoke(element) ?: closingTags[element.tagName()] ?: ""
    }


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

}