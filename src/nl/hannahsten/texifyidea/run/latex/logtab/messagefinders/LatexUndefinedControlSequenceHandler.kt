package nl.hannahsten.texifyidea.run.latex.logtab.messagefinders

import nl.hannahsten.texifyidea.run.latex.logtab.LatexLogMessage
import nl.hannahsten.texifyidea.run.latex.logtab.LatexLogMessageType
import nl.hannahsten.texifyidea.run.latex.logtab.LatexMessageHandler

object LatexUndefinedControlSequenceHandler : LatexMessageHandler(
        """(^(?<file>.+)?:(?<line>\d+): (?<message>Undefined control sequence.)\s*l.\d+\s*(?<command>\\\w+)$)""".toRegex(),
        """(! LaTeX Error: (?<message>Undefined control sequence.)\s*l.\d+\s*(?<command>\\\w+)${'$'})""".toRegex()
) {
    override fun findMessage(text: String, newText: String, currentFile: String?): LatexLogMessage? {
        regex.forEach {
            it.find(text)?.apply {
                val (line, fileName) = try {
                    Pair(groups["line"]?.value?.toInt(), groups["file"]?.value?.trim() ?: currentFile)
                } catch (e: IllegalArgumentException) {
                    Pair(null, currentFile)
                }

                val message = "${groups["message"]?.value} ${groups["command"]?.value}"
                return LatexLogMessage(message, fileName, line, LatexLogMessageType.ERROR)
            }
        }
        return null
    }
}