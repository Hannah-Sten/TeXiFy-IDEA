package nl.hannahsten.texifyidea.editor.pasteproviders

import nl.hannahsten.texifyidea.file.LatexFile
import org.jsoup.nodes.Element

class StyledTextHtmlToLatexConverter : HtmlToLatexConverter {

    companion object {

        /**
         * Map HTML tags to LaTeX
         */
        val openingTags = hashMapOf(
            "b" to "\\textbf{",
            "body" to "",
            "br" to "\n",
            "em" to "\\textit{",
            "h1" to "\\chapter*{",
            "h2" to "\\section*{",
            "h3" to "\\subsection*{",
            "h4" to "\\subsubsection*{",
            "h5" to "\\subsubsubsection*{",
            "i" to "\\textit{",
            "li" to "\\item ",
            "ol" to "\\begin{enumerate}\n",
            "p" to "",
            "sub" to "\\textsubscript{",
            "sup" to "\\textsuperscript{",
            "u" to "\\underline{",
            "ul" to "\\begin{itemize}\n",
        )

        val closingTags = hashMapOf(
            "a" to "}",
            "b" to "}",
            "body" to "",
            "br" to "",
            "div" to "\n",
            "em" to "}",
            "h1" to "}\n",
            "h2" to "}\n",
            "h3" to "}\n",
            "h4" to "}\n",
            "h5" to "}\n",
            "i" to "}",
            "li" to "\n",
            "ol" to "\\end{enumerate}\n",
            "p" to "\n\n",
            "sub" to "}",
            "sup" to "}",
            "u" to "}",
            "ul" to "\\end{itemize}\n",
        )
    }

    override fun convertHtmlToLatex(htmlIn: Element, file: LatexFile): String {
        var latexString = ""

        val prefix = getPrefix(htmlIn)

        val content = if (htmlIn.childNodeSize() > 0)
            convertHtmlToLatex(htmlIn.childNodes(), file)
        else
            htmlIn.text()

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
                    "\\href{" + element.attr("href") + "}{"
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