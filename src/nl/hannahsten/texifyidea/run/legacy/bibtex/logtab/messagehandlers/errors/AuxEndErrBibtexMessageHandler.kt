package nl.hannahsten.texifyidea.run.legacy.bibtex.logtab.messagehandlers.errors

import nl.hannahsten.texifyidea.run.legacy.bibtex.logtab.BibtexLogMagicRegex
import nl.hannahsten.texifyidea.run.legacy.bibtex.logtab.BibtexLogMessage
import nl.hannahsten.texifyidea.run.legacy.bibtex.logtab.BibtexLogMessageType
import nl.hannahsten.texifyidea.run.legacy.bibtex.logtab.BibtexMessageHandler

/**
 * Handle log messages which use the procedure aux_end_err in bibtex.web (line 3352).
 */
object AuxEndErrBibtexMessageHandler : BibtexMessageHandler() {

    override fun findMessage(window: List<String>, currentFile: String): BibtexLogMessage? {
        BibtexLogMagicRegex.auxEndErr.find(window.lastOrNull() ?: "")?.apply {
            return BibtexLogMessage(groups["message"]?.value ?: window.last(), groups["file"]?.value, null, BibtexLogMessageType.ERROR)
        }
        return null
    }
}