package nl.hannahsten.texifyidea.run.latex.logtab.messagehandlers.warnings

import nl.hannahsten.texifyidea.run.latex.logtab.LatexLogMagicRegex.pdfTeXWarning
import nl.hannahsten.texifyidea.run.latex.logtab.LatexLogMessage
import nl.hannahsten.texifyidea.run.latex.logtab.LatexLogMessageType
import nl.hannahsten.texifyidea.run.latex.logtab.LatexMessageHandler

/**
 * pdfTeX warning messages (do not necessarily start on a new line)
 */
object LatexPdftexWarningMessageHandler : LatexMessageHandler(
    LatexLogMessageType.WARNING,
    pdfTeXWarning
) {

    override fun findMessage(text: String, newText: String, currentFile: String?): LatexLogMessage? {
        regex.forEach {
            it.find(text)?.apply {
                val message = groups["message"]?.value ?: return null
                return LatexLogMessage(message, currentFile, -1, messageType)
            }
        }
        return null
    }
}