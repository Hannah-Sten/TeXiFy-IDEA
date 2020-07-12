package nl.hannahsten.texifyidea.run.bibtex.logtab.messagehandlers.warnings

import nl.hannahsten.texifyidea.run.bibtex.logtab.BibtexLogMagicRegex
import nl.hannahsten.texifyidea.run.bibtex.logtab.BibtexLogMessage
import nl.hannahsten.texifyidea.run.bibtex.logtab.BibtexLogMessageType
import nl.hannahsten.texifyidea.run.bibtex.logtab.BibtexMessageHandler

/**
 * Handle log messages which use the procedure bib_ln_num_print on line 5045 in bibtex.web.
 * Note that warnings have '--' before 'line', and errors '---' (see bib_err_print vs bib_warn_print).
 */
object BibLnNumPrintBibtexMessageHandler : BibtexMessageHandler() {
    override fun findMessage(window: List<String>, currentFile: String): BibtexLogMessage? {
        BibtexLogMagicRegex.bibLnNumPrint.find(window.lastOrNull() ?: "")?.apply {
            if (window.size < 2) return null
            return BibtexLogMessage(window[window.size - 2], groups["file"]?.value, groups["line"]?.value?.toInt(), BibtexLogMessageType.ERROR)
        }
        return null
    }
}