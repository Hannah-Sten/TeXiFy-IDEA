package nl.hannahsten.texifyidea.run.ui.console.logtab.messagehandlers.errors

import nl.hannahsten.texifyidea.run.ui.console.logtab.LatexLogMessage
import nl.hannahsten.texifyidea.run.ui.console.logtab.LatexLogMessageType
import nl.hannahsten.texifyidea.run.ui.console.logtab.LatexMessageHandler


<<<<<<<< HEAD:src/nl/hannahsten/texifyidea/run/ui/console/logtab/messagehandlers/errors/LatexFixMeErrorMessageHandler.kt
import nl.hannahsten.texifyidea.run.ui.console.logtab.LatexLogMagicRegex.fixMeError
import nl.hannahsten.texifyidea.run.ui.console.logtab.LatexLogMessage
import nl.hannahsten.texifyidea.run.ui.console.logtab.LatexLogMessageType
import nl.hannahsten.texifyidea.run.ui.console.logtab.LatexMessageHandler
========
import nl.hannahsten.texifyidea.run.latex.logtab.LatexLogMessage
import nl.hannahsten.texifyidea.run.latex.logtab.LatexLogMessageType
import nl.hannahsten.texifyidea.run.latex.logtab.LatexMessageHandler
>>>>>>>> master:src/nl/hannahsten/texifyidea/run/ui/console/logtab/messagehandlers/errors/LatexFixMeMessageHandler.kt

/**
 * Errors by the fix me package.
 */
class LatexFixMeMessageHandler(messageType: LatexLogMessageType) : LatexMessageHandler(
    messageType,
    """FiXme (Fatal )?${if (messageType == LatexLogMessageType.ERROR) "Error" else "Warning"}: '(?<message>.+)' on input line (?<line>\d+).""".toRegex()
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