package nl.hannahsten.texifyidea.run.latex.logtab

abstract class LatexMessageHandler(vararg val regex: Regex) {
    abstract fun findMessage(text: String, newText: String, currentFile: String?): LatexLogMessage?

}