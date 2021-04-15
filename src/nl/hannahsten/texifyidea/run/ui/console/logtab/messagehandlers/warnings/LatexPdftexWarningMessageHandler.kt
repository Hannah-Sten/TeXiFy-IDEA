package nl.hannahsten.texifyidea.run.ui.console.logtab.messagehandlers.warnings

import nl.hannahsten.texifyidea.run.ui.console.logtab.LatexLogMagicRegex.pdfTeXWarning
import nl.hannahsten.texifyidea.run.ui.console.logtab.LatexLogMessage
import nl.hannahsten.texifyidea.run.ui.console.logtab.LatexLogMessageType
import nl.hannahsten.texifyidea.run.ui.console.logtab.LatexMessageHandler

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