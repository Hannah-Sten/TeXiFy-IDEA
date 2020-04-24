package nl.hannahsten.texifyidea.run.latex.logtab

open class LatexMessageHandler(val messageType: LatexLogMessageType, vararg val regex: Regex) {
    open fun findMessage(text: String, newText: String, currentFile: String?): LatexLogMessage? {
        regex.forEach {
            it.find(text)?.apply {
                return LatexLogMessage(this.value, fileName = currentFile, type = messageType)
            }
        }
        return null
    }
}