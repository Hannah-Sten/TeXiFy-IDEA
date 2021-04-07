package nl.hannahsten.texifyidea.run.latex.logtab.messagehandlers.warnings

import nl.hannahsten.texifyidea.run.latex.logtab.LatexLogMagicRegex
import nl.hannahsten.texifyidea.run.latex.logtab.LatexLogMessage
import nl.hannahsten.texifyidea.run.latex.logtab.LatexLogMessageType.WARNING
import nl.hannahsten.texifyidea.run.latex.logtab.LatexMessageHandler

/**
 * Warnings of the form LaTeX Warning: ... on input line l
 */
object LatexLineWarningHandler : LatexMessageHandler(
    WARNING,
    """${LatexLogMagicRegex.LATEX_WARNING_REGEX}(?<message>.+)${LatexLogMagicRegex.LINE_REGEX}""".toRegex()
) {

    override fun findMessage(text: String, newText: String, currentFile: String?): LatexLogMessage? {
        LatexLineWarningHandler.regex.forEach {
            it.find(text)?.apply {
                val message = groups["message"]?.value?.trim() ?: return@apply
                val line = groups["line"]?.value?.toInt() ?: return@apply
                return LatexLogMessage(message.replace("(Font)             ", ","), fileName = currentFile, type = super.messageType, line = line)
            }
        }
        return null
    }
}