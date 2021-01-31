package nl.hannahsten.texifyidea.run.bibtex.logtab.messagehandlers.warnings

import nl.hannahsten.texifyidea.run.bibtex.logtab.BibtexLogMagicRegex
import nl.hannahsten.texifyidea.run.bibtex.logtab.BibtexLogMessage
import nl.hannahsten.texifyidea.run.bibtex.logtab.BibtexLogMessageType
import nl.hannahsten.texifyidea.run.bibtex.logtab.BibtexMessageHandler

/**
 * Handle biber warning messages which do have information about the line number.
 */
object BiberWarningBibtexSubsystemMessageHandler : BibtexMessageHandler() {

    override fun findMessage(window: List<String>, currentFile: String): BibtexLogMessage? {
        BibtexLogMagicRegex.biberWarningBibtexSubsystem.find(window.lastOrNull() ?: "")?.apply {
            return BibtexLogMessage(groups["message"]?.value ?: "", currentFile, groups["line"]?.value?.toInt(), BibtexLogMessageType.WARNING)
        }
        return null
    }
}