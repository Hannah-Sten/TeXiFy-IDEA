package nl.hannahsten.texifyidea.editor.pasteproviders

import com.intellij.openapi.actionSystem.DataContext
import org.jsoup.nodes.Node

interface LatexPasteProvider {

    /**
     * Translate HTML (e.g. from a clipboard) to LaTeX.
     */
    fun translateHtml(htmlIn: Node, dataContext: DataContext): String
}