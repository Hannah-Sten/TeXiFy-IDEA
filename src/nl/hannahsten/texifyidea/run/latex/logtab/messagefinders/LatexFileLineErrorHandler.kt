package nl.hannahsten.texifyidea.run.latex.logtab.messagefinders

import nl.hannahsten.texifyidea.run.latex.logtab.LatexLogMessage
import nl.hannahsten.texifyidea.run.latex.logtab.LatexLogMessageType
import nl.hannahsten.texifyidea.run.latex.logtab.LatexMessageHandler
import nl.hannahsten.texifyidea.util.removeAll

object LatexFileLineErrorHandler : LatexMessageHandler(
        """(^(?<file>.+)?:(?<line>\d+): (?<message>.+)$)""".toRegex(),
        """(! LaTeX Error: (?<message>.+)${'$'})""".toRegex()
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
                return LatexLogMessage(message, fileName, line, LatexLogMessageType.ERROR)
            }
        }
        return null
    }
}