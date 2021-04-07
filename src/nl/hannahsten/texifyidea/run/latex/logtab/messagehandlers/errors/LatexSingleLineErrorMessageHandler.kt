package nl.hannahsten.texifyidea.run.latex.logtab.messagehandlers.errors

import nl.hannahsten.texifyidea.run.latex.logtab.LatexLogMagicRegex.FILE_LINE_REGEX
import nl.hannahsten.texifyidea.run.latex.logtab.LatexLogMessage
import nl.hannahsten.texifyidea.run.latex.logtab.LatexLogMessageType
import nl.hannahsten.texifyidea.run.latex.logtab.LatexMessageHandler

/**
 * Single-line errors, for which the l.<line number> appears in 'text' but should be excluded for the error message.
 */
object LatexSingleLineErrorMessageHandler : LatexMessageHandler(
    LatexLogMessageType.ERROR,
    """^$FILE_LINE_REGEX (?<message>.+)\s*l\.\d+""".toRegex()
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

                val message = groups["message"]?.value ?: return null
                return LatexLogMessage(message, fileName, line ?: 1, messageType)
            }
        }
        return null
    }
}