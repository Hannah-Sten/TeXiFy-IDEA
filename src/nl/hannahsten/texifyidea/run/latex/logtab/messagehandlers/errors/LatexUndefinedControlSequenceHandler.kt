package nl.hannahsten.texifyidea.run.latex.logtab.messagehandlers.errors

import nl.hannahsten.texifyidea.run.latex.logtab.LatexLogMagicRegex.FILE_LINE_REGEX
import nl.hannahsten.texifyidea.run.latex.logtab.LatexLogMagicRegex.LATEX_ERROR_REGEX
import nl.hannahsten.texifyidea.run.latex.logtab.LatexLogMessage
import nl.hannahsten.texifyidea.run.latex.logtab.LatexLogMessageType
import nl.hannahsten.texifyidea.run.latex.logtab.LatexMessageHandler

object LatexUndefinedControlSequenceHandler : LatexMessageHandler(
    LatexLogMessageType.ERROR,
    // The last part (with line number and command) is optional because it may appear on the next line
    // Any line content may appear before the command which is undefined (which is the last thing on the line)
    """^$FILE_LINE_REGEX (?<message>Undefined control sequence.)(\s*l.\d+[\s\S]*(?<command>\\\w+)$)?""".toRegex(),
    """^$LATEX_ERROR_REGEX (?<message>Undefined control sequence\.)\s*l\.(?<line>\d+)\s*.*(?<command>\\\w+)${'$'}""".toRegex()
) {

    override fun findMessage(text: String, newText: String, currentFile: String?): LatexLogMessage? {
        regex.forEach {
            it.find(text)?.apply {
                val line = try {
                    groups["line"]?.value?.toInt()
                }
                catch (ignored: IllegalArgumentException) {
                    null
                }
                val fileName = try {
                    groups["file"]?.value?.trim()
                }
                catch (ignored: IllegalArgumentException) {
                    currentFile
                }

                val message = "${groups["message"]?.value} ${groups["command"]?.value ?: ""}"

                return LatexLogMessage(message, fileName, line ?: 1, messageType)
            }
        }
        return null
    }
}