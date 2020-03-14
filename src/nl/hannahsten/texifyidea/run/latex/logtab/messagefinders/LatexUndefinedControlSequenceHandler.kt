package nl.hannahsten.texifyidea.run.latex.logtab.messagefinders

import nl.hannahsten.texifyidea.run.latex.logtab.LatexLogMessage
import nl.hannahsten.texifyidea.run.latex.logtab.LatexLogMessageType
import nl.hannahsten.texifyidea.run.latex.logtab.LatexMessageHandler

object LatexUndefinedControlSequenceHandler : LatexMessageHandler(
        """^(?<file>.+)?:(?<line>\d+): (?<message>Undefined control sequence.)\s*l.\d+\s*(?<command>\\\w+)$""".toRegex()
) {
    override fun findMessage(text: String, newText: String, currentFile: String?): LatexLogMessage? {
        regex.find(text)?.apply {
            val line = groups["line"]?.value?.toInt()
            val fileName = groups["file"]?.value?.trim() ?: currentFile
            val message = "${groups["message"]?.value} ${groups["command"]?.value}"
            return LatexLogMessage(message, fileName, line, LatexLogMessageType.ERROR)
        }
        return null
    }
}