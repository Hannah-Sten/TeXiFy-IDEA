package nl.hannahsten.texifyidea.run.bibtex.logtab.messagehandlers.errors

import nl.hannahsten.texifyidea.run.bibtex.logtab.BibtexLogMagicRegex
import nl.hannahsten.texifyidea.run.bibtex.logtab.BibtexLogMessage
import nl.hannahsten.texifyidea.run.bibtex.logtab.BibtexLogMessageType
import nl.hannahsten.texifyidea.run.bibtex.logtab.BibtexMessageHandler

/**
 * Handle log messages which use the procedure nonexistent_cross_reference_error on line 6617 of bibtex.web.
 * Note that it consists of two lines, without any indication that it does.
 */
object NonexistentCrossReferenceBibtexMessageHandler : BibtexMessageHandler() {

    override fun findMessage(window: List<String>, currentFile: String): BibtexLogMessage? {
        BibtexLogMagicRegex.nonexistentCrossReferenceError.find(window.lastOrNull() ?: "")?.apply {
            if (window.size < 2) return null
            return BibtexLogMessage(window.takeLast(2).joinToString(" "), currentFile, null, BibtexLogMessageType.ERROR)
        }
        return null
    }
}