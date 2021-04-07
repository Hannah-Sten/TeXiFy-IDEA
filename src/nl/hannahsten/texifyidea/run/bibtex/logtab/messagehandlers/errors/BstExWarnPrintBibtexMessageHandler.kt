package nl.hannahsten.texifyidea.run.bibtex.logtab.messagehandlers.errors

import nl.hannahsten.texifyidea.run.bibtex.logtab.BibtexLogMagicRegex
import nl.hannahsten.texifyidea.run.bibtex.logtab.BibtexLogMessage
import nl.hannahsten.texifyidea.run.bibtex.logtab.BibtexLogMessageType
import nl.hannahsten.texifyidea.run.bibtex.logtab.BibtexMessageHandler

/**
 * Handle log messages which use the procedure bst_ex_warn_print on line 6874 of bibtex.web.
 * Even though it shows a line and file, this is from the bst file and most likely not the problem.
 */
object BstExWarnPrintBibtexMessageHandler : BibtexMessageHandler() {

    override fun findMessage(window: List<String>, currentFile: String): BibtexLogMessage? {
        BibtexLogMagicRegex.bstExWarnPrint.find(window.lastOrNull() ?: "")?.apply {
            if (window.size < 2) return null
            return BibtexLogMessage(window[window.size - 2], currentFile, null, BibtexLogMessageType.ERROR)
        }
        return null
    }
}