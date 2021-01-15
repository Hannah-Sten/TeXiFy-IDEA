package nl.hannahsten.texifyidea.run.bibtex.logtab.messagehandlers.warnings

import nl.hannahsten.texifyidea.run.bibtex.logtab.BibtexLogMagicRegex
import nl.hannahsten.texifyidea.run.bibtex.logtab.BibtexLogMessage
import nl.hannahsten.texifyidea.run.bibtex.logtab.BibtexLogMessageType
import nl.hannahsten.texifyidea.run.bibtex.logtab.BibtexMessageHandler

/**
 * Handle log messages which use line 3937 in bibtex.web, which is a special case where 'Warning--' and '--line' appear on the same line.
 */
object NoFieldsBibtexMessageHandler : BibtexMessageHandler() {

    override fun findMessage(window: List<String>, currentFile: String): BibtexLogMessage? {
        // This case is handled by WarningBibtexMessageHandler
        BibtexLogMagicRegex.noFields.find(window.lastOrNull() ?: "")?.apply {
            return BibtexLogMessage(groups["message"]?.value ?: "", groups["file"]?.value, groups["line"]?.value?.toInt(), BibtexLogMessageType.WARNING)
        }
        return null
    }
}