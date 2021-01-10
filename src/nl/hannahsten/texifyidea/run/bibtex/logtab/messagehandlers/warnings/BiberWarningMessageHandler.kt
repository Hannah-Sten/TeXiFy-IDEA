package nl.hannahsten.texifyidea.run.bibtex.logtab.messagehandlers.warnings

import nl.hannahsten.texifyidea.run.bibtex.logtab.BibtexLogMagicRegex
import nl.hannahsten.texifyidea.run.bibtex.logtab.BibtexLogMessage
import nl.hannahsten.texifyidea.run.bibtex.logtab.BibtexLogMessageType
import nl.hannahsten.texifyidea.run.bibtex.logtab.BibtexMessageHandler

/**
 * Handle biber warning messages which do not have any information about file or line.
 */
object BiberWarningMessageHandler : BibtexMessageHandler() {

    override fun findMessage(window: List<String>, currentFile: String): BibtexLogMessage? {
        val regexes = listOf(BibtexLogMagicRegex.biberWarningInFile, BibtexLogMagicRegex.biberWarning)
        regexes.forEach { regex ->
            regex.find(window.lastOrNull() ?: "")?.apply {
                return BibtexLogMessage(groups["message"]?.value ?: "", currentFile, null, BibtexLogMessageType.WARNING)
            }
        }
        return null
    }
}