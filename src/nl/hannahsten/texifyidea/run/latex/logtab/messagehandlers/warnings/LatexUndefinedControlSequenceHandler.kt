package nl.hannahsten.texifyidea.run.latex.logtab.messagehandlers.warnings

import nl.hannahsten.texifyidea.run.latex.logtab.LatexLogMessage
import nl.hannahsten.texifyidea.run.latex.logtab.LatexLogMessageType
import nl.hannahsten.texifyidea.run.latex.logtab.LatexMessageHandler
import nl.hannahsten.texifyidea.run.latex.logtab.LogMagicRegex.FILE_LINE_REGEX
import nl.hannahsten.texifyidea.run.latex.logtab.LogMagicRegex.LATEX_ERROR_REGEX

object LatexUndefinedControlSequenceHandler : LatexMessageHandler(
        LatexLogMessageType.ERROR,
        """^$FILE_LINE_REGEX (?<message>Undefined control sequence.)\s*l.\d+\s*(?<command>\\\w+)$""".toRegex(),
        """^$LATEX_ERROR_REGEX (?<message>Undefined control sequence.)\s*l.\d+\s*(?<command>\\\w+)$""".toRegex()
) {
    override fun findMessage(text: String, newText: String, currentFile: String?): LatexLogMessage? {
        regex.forEach {
            it.find(text)?.apply {
                val (line, fileName) = try {
                    Pair(groups["line"]?.value?.toInt(), groups["file"]?.value?.trim() ?: currentFile)
                }
                catch (e: IllegalArgumentException) {
                    Pair(null, currentFile)
                }

                val message = "${groups["message"]?.value} ${groups["command"]?.value}"
                return LatexLogMessage(message, fileName, line ?: 1, messageType)
            }
        }
        return null
    }
}