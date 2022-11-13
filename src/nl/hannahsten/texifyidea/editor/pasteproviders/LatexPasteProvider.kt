package nl.hannahsten.texifyidea.editor.pasteproviders

import com.intellij.openapi.actionSystem.DataContext
import org.jsoup.nodes.Node

interface LatexPasteProvider {
    fun translateHtml(htmlIn: Node, dataContext: DataContext): String
}