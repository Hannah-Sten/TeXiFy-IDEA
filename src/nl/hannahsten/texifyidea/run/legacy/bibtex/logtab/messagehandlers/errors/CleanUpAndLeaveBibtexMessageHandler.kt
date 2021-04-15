package nl.hannahsten.texifyidea.run.legacy.bibtex.logtab.messagehandlers.errors

import nl.hannahsten.texifyidea.run.legacy.bibtex.logtab.BibtexLogMagicRegex
import nl.hannahsten.texifyidea.run.legacy.bibtex.logtab.BibtexLogMessage
import nl.hannahsten.texifyidea.run.legacy.bibtex.logtab.BibtexLogMessageType
import nl.hannahsten.texifyidea.run.legacy.bibtex.logtab.BibtexMessageHandler

/**
 * Handle log messages which use the Clean up and leave macro in bibtex.web (line 11324)
 * and for which we assume the message is on the previous line.
 */
object CleanUpAndLeaveBibtexMessageHandler : BibtexMessageHandler() {

    override fun findMessage(window: List<String>, currentFile: String): BibtexLogMessage? {
        BibtexLogMagicRegex.cleanUpAndLeave.find(window.lastOrNull() ?: "")?.apply {
            if (window.size < 2) return null
            return BibtexLogMessage(window[window.size - 2], groups["file"]?.value, groups["line"]?.value?.toInt(), BibtexLogMessageType.ERROR)
        }
        return null
    }
}