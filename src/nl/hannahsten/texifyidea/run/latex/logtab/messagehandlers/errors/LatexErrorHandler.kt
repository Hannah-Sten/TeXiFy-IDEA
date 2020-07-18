package nl.hannahsten.texifyidea.run.latex.logtab.messagehandlers.errors

import nl.hannahsten.texifyidea.run.latex.logtab.LatexLogMagicRegex.FILE_LINE_REGEX
import nl.hannahsten.texifyidea.run.latex.logtab.LatexLogMagicRegex.LATEX_ERROR_REGEX
import nl.hannahsten.texifyidea.run.latex.logtab.LatexLogMagicRegex.PDFTEX_ERROR_REGEX
import nl.hannahsten.texifyidea.run.latex.logtab.LatexLogMessage
import nl.hannahsten.texifyidea.run.latex.logtab.LatexLogMessageType
import nl.hannahsten.texifyidea.run.latex.logtab.LatexMessageHandler

object LatexErrorHandler : LatexMessageHandler(
        LatexLogMessageType.ERROR,
        """^$FILE_LINE_REGEX (?<message>.+)""".toRegex(),
        """^$LATEX_ERROR_REGEX (?<message>.+)""".toRegex(),
        """^$PDFTEX_ERROR_REGEX (?<message>.+)""".toRegex()
) {
    private val messageProcessors = listOf(LatexPackageErrorProcessor, LatexRemoveErrorTextProcessor)

    override fun findMessage(text: String, newText: String, currentFile: String?): LatexLogMessage? {
        regex.forEach {
            it.find(text)?.apply {
                val (line, fileName) = try {
                    Pair(groups["line"]?.value?.toInt(), groups["file"]?.value?.trim() ?: currentFile)
                }
                catch (e: IllegalArgumentException) {
                    Pair(null, currentFile)
                }

                val message = groups["message"]?.value?.trim() ?: ""

                // Process a found error message (e.g. remove "LaTeX Error:")
                val processedMessage = messageProcessors.mapNotNull { p ->
                    if (p.regex.any { r -> r.containsMatchIn(message) }) p.postProcess(p.process(message)) else null
                }
                    .firstOrNull() ?: message
                    .replace("<inserted text>", "")
                    .replace("<to be read again>", "")

                val trimmedMessage = processedMessage.replace("\\s+".toRegex(), " ")

                return LatexLogMessage(trimmedMessage, fileName, line ?: -1, messageType)
            }
        }
        return null
    }
}