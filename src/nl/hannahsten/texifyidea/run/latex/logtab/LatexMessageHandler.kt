package nl.hannahsten.texifyidea.run.latex.logtab

abstract class LatexMessageHandler(val regex: Regex) {
    abstract fun findMessage(text: String, newText: String, currentFile: String?): LatexLogMessage?

}