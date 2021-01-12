package nl.hannahsten.texifyidea.run.latex.logtab.messagehandlers.errors

import nl.hannahsten.texifyidea.run.latex.logtab.LatexLogMagicRegex.fixMeError
import nl.hannahsten.texifyidea.run.latex.logtab.LatexLogMessage
import nl.hannahsten.texifyidea.run.latex.logtab.LatexLogMessageType
import nl.hannahsten.texifyidea.run.latex.logtab.LatexMessageHandler

/**
 * Errors by the fix me package.
 */
object LatexFixMeErrorMessageHandler : LatexMessageHandler(
    LatexLogMessageType.ERROR,
    fixMeError
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