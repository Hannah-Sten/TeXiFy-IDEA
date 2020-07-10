package nl.hannahsten.texifyidea.run.bibtex.logtab.messagehandlers.errors

import nl.hannahsten.texifyidea.run.bibtex.logtab.BibtexLogMagicRegex
import nl.hannahsten.texifyidea.run.bibtex.logtab.BibtexLogMessage
import nl.hannahsten.texifyidea.run.bibtex.logtab.BibtexLogMessageType
import nl.hannahsten.texifyidea.run.bibtex.logtab.BibtexMessageHandler

/**
 * Handle log messages which use the procedure aux_err_print in bibtex.web (line 2698)
 * and for which we assume the message is on the line previous to the line starting with ---line
 * as in the aux_err procedure on line 2690.
 */
object AuxErrPrintBibtexMessageHandler : BibtexMessageHandler() {
    override fun findMessage(window: List<String>): BibtexLogMessage? {
        BibtexLogMagicRegex.auxErrPrint.find(window.lastOrNull() ?: "")?.apply {
            if (window.size < 2) return null
            return BibtexLogMessage(window[window.size - 2], groups["file"]?.value, groups["line"]?.value?.toInt(), BibtexLogMessageType.ERROR)
        }
        return null
    }
}