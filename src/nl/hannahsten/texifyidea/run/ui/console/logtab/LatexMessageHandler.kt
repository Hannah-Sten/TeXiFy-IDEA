package nl.hannahsten.texifyidea.run.ui.console.logtab

open class LatexMessageHandler(open val messageType: LatexLogMessageType, open vararg val regex: Regex) {

    open fun findMessage(text: String, newText: String, currentFile: String?): LatexLogMessage? {
        regex.forEach {
            it.find(text)?.apply {
                return LatexLogMessage(this.value, fileName = currentFile, type = messageType)
            }
        }
        return null
    }
}