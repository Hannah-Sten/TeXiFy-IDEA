package nl.hannahsten.texifyidea.run.ui.console.logtab.messagehandlers.warnings

import nl.hannahsten.texifyidea.run.ui.console.logtab.LatexLogMagicRegex.fixMeWarning
import nl.hannahsten.texifyidea.run.ui.console.logtab.LatexLogMessage
import nl.hannahsten.texifyidea.run.ui.console.logtab.LatexLogMessageType
import nl.hannahsten.texifyidea.run.ui.console.logtab.LatexMessageHandler

/**
 * Warnings by the fix me package.
 */
object LatexFixMeWarningMessageHandler : LatexMessageHandler(
    LatexLogMessageType.WARNING,
    fixMeWarning
) {

    override fun findMessage(text: String, newText: String, currentFile: String?): LatexLogMessage? {
        regex.forEach {
            it.find(text)?.apply {
                val message = groups["message"]?.value ?: return null
                return LatexLogMessage("FiXme: $message", currentFile, groups["line"]?.value?.toInt() ?: -1, messageType)
            }
        }
        return null
    }
}