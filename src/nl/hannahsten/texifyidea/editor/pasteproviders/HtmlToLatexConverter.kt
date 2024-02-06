package nl.hannahsten.texifyidea.editor.pasteproviders

import nl.hannahsten.texifyidea.file.LatexFile
import org.jsoup.nodes.Element

/**
 * Convert html to LaTeX, for example styled text, images or tables.
 */
interface HtmlToLatexConverter {

    /**
     * Translate HTML (e.g. from a clipboard) to LaTeX.
     */
    fun convertHtmlToLatex(htmlIn: Element, file: LatexFile): String
}