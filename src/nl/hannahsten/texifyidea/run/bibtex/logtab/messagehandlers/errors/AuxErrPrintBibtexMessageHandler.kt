package nl.hannahsten.texifyidea.run.bibtex.logtab.messagehandlers.errors

import nl.hannahsten.texifyidea.run.bibtex.logtab.BibtexLogMagicRegex
import nl.hannahsten.texifyidea.run.bibtex.logtab.BibtexLogMessage
import nl.hannahsten.texifyidea.run.bibtex.logtab.BibtexLogMessageType
import nl.hannahsten.texifyidea.run.bibtex.logtab.BibtexMessageHandler

/**
 * Handle log messages which use the procedure aux_err_print in bibtex.web (line 2698).
 * The actual message will probably be directly before the ---line but may be on the same or previous line.
 */
object AuxErrPrintBibtexMessageHandler : BibtexMessageHandler() {

    override fun findMessage(window: List<String>, currentFile: String): BibtexLogMessage? {
        BibtexLogMagicRegex.auxErrPrint.find(window.lastOrNull() ?: "")?.apply {
            if (window.size < 2) return null

            // Check if the message is on the same or previous line
            val message = if (this.range.first > 0) {
                window.last().substring(0, this.range.first)
            }
            else {
                window[window.size - 2]
            }
            return BibtexLogMessage(message, groups["file"]?.value, groups["line"]?.value?.toInt(), BibtexLogMessageType.ERROR)
        }
        return null
    }
}