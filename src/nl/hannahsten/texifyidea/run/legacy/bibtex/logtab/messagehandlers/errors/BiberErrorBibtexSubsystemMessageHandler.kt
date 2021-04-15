package nl.hannahsten.texifyidea.run.legacy.bibtex.logtab.messagehandlers.errors

import nl.hannahsten.texifyidea.run.legacy.bibtex.logtab.BibtexLogMagicRegex
import nl.hannahsten.texifyidea.run.legacy.bibtex.logtab.BibtexLogMessage
import nl.hannahsten.texifyidea.run.legacy.bibtex.logtab.BibtexLogMessageType
import nl.hannahsten.texifyidea.run.legacy.bibtex.logtab.BibtexMessageHandler

/**
 * Handle biber error messages with file and line information.
 */
object BiberErrorBibtexSubsystemMessageHandler : BibtexMessageHandler() {

    override fun findMessage(window: List<String>, currentFile: String): BibtexLogMessage? {
        BibtexLogMagicRegex.biberErrorBibtexSubsystem.find(window.lastOrNull() ?: "")?.apply {
            // Though we have the name of the auxiliary file, we just assume it is the same as the opened bib file
            // Also note that the line is not exact (because in the auxiliary file) but hopefully close
            return BibtexLogMessage(groups["message"]?.value ?: "", currentFile, groups["line"]?.value?.toInt(), BibtexLogMessageType.ERROR)
        }
        return null
    }
}