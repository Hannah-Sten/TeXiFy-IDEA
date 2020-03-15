package nl.hannahsten.texifyidea.run.latex.logtab.messagehandlers

import nl.hannahsten.texifyidea.run.latex.logtab.LatexLogMessage
import nl.hannahsten.texifyidea.run.latex.logtab.LatexLogMessageType
import nl.hannahsten.texifyidea.run.latex.logtab.LatexMessageHandler
import nl.hannahsten.texifyidea.util.removeAll

object LatexErrorHandler : LatexMessageHandler(
        LatexLogMessageType.ERROR,
        """^$FILE_LINE_REGEX (?<message>.+)$""".toRegex(),
        """^$LATEX_ERROR_REGEX (?<message>.+)${'$'}$""".toRegex()
) {
    override fun findMessage(text: String, newText: String, currentFile: String?): LatexLogMessage? {
        regex.forEach {
            it.find(text)?.apply {
                val (line, fileName) = try {
                    Pair(groups["line"]?.value?.toInt(), groups["file"]?.value?.trim() ?: currentFile)
                } catch (e: IllegalArgumentException) {
                    Pair(null, currentFile)
                }

                val message = groups["message"]?.value?.removeSuffix(newText)
                        ?.removeAll("LaTeX Error:")?.trim() ?: ""
                return LatexLogMessage(message, fileName, line, messageType)
            }
        }
        return null
    }
}