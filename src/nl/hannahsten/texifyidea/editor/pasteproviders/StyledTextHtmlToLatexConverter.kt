package nl.hannahsten.texifyidea.editor.pasteproviders

import nl.hannahsten.texifyidea.file.LatexFile
import org.jsoup.nodes.Element
import org.jsoup.nodes.Node

class StyledTextHtmlToLatexConverter : HtmlToLatexConverter {

    companion object {

        /**
         * Map HTML tags to LaTeX
         */
        val openingTags = hashMapOf(
            "i" to "\\textit{",
            "em" to "\\textit{",
            "b" to "\\textbf{",
            "u" to "\\underline{",
            "p" to "",
            "body" to "",
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
            "em" to "}",
            "b" to "}",
            "u" to "}",
            "p" to "\n\n",
            "body" to "",
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

        val prefix = getPrefix(htmlIn)

        val content = if (htmlIn.childNodeSize() > 0)
            convertHtmlToLatex(htmlIn.childNodes(), file)
        else
            escapeText(htmlIn.text())

        val postfix = getPostfix(htmlIn)

        // Special case: since the <img> tag will trigger the InsertGraphicWizardAction, the image maybe be placed in a figure environment so we don't want to put that in a href
        latexString += if (htmlIn.tagName() == "a" && (htmlIn.childNodes().firstOrNull() as? Element)?.tagName() == "img") {
            content
        }
        else {
            prefix + content + postfix
        }
        return latexString
    }

    private fun getPrefix(element: Element): String {
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