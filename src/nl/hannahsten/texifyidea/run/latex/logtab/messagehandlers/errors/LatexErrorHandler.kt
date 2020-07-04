package nl.hannahsten.texifyidea.run.latex.logtab.messagehandlers.errors

import nl.hannahsten.texifyidea.run.latex.logtab.LatexLogMessage
import nl.hannahsten.texifyidea.run.latex.logtab.LatexLogMessageType
import nl.hannahsten.texifyidea.run.latex.logtab.LatexMessageHandler
import nl.hannahsten.texifyidea.run.latex.logtab.LogMagicRegex.FILE_LINE_REGEX
import nl.hannahsten.texifyidea.run.latex.logtab.LogMagicRegex.LATEX_ERROR_REGEX
import nl.hannahsten.texifyidea.run.latex.logtab.LogMagicRegex.PDFTEX_ERROR_REGEX

object LatexErrorHandler : LatexMessageHandler(
        LatexLogMessageType.ERROR,
        """^$FILE_LINE_REGEX (?<message>.+)$""".toRegex(),
        """^$LATEX_ERROR_REGEX (?<message>.+)${'$'}$""".toRegex(),
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
                    if (p.regex.any { r -> r.containsMatchIn(message) }) p.process(message) else null
                }
                    .firstOrNull() ?: message
                    .replace("<inserted text>", "")

                return LatexLogMessage(processedMessage, fileName, line ?: 1, messageType)
            }
        }
        return null
    }
}