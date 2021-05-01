package nl.hannahsten.texifyidea.run.ui.console.logtab

/**
 * A [LatexMessageHandler] but assuming that the last regex group is the source line number.
 */
open class LatexMessageWithLineHandler(override val messageType: LatexLogMessageType, override vararg val regex: Regex) : LatexMessageHandler(messageType, *regex) {

    override fun findMessage(text: String, newText: String, currentFile: String?): LatexLogMessage? {
        regex.first().find(text)?.apply {
            val message = this.value
            val line = groupValues.drop(1).dropLastWhile { it.isBlank() }.lastOrNull()?.toInt() ?: 1
            return LatexLogMessage(
                message, currentFile, line,
                messageType
            )
        }
        return null
    }
}