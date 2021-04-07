package nl.hannahsten.texifyidea.run.bibtex.logtab.messagehandlers.errors

import nl.hannahsten.texifyidea.run.bibtex.logtab.BibtexLogMagicRegex
import nl.hannahsten.texifyidea.run.bibtex.logtab.BibtexLogMessage
import nl.hannahsten.texifyidea.run.bibtex.logtab.BibtexLogMessageType
import nl.hannahsten.texifyidea.run.bibtex.logtab.BibtexMessageHandler

/**
 * Handle biber error messages which do not have any information about file or line.
 */
object BiberErrorMessageHandler : BibtexMessageHandler() {

    override fun findMessage(window: List<String>, currentFile: String): BibtexLogMessage? {
        BibtexLogMagicRegex.biberError.find(window.lastOrNull() ?: "")?.apply {
            return BibtexLogMessage(groups["message"]?.value ?: "", currentFile, null, BibtexLogMessageType.ERROR)
        }
        return null
    }
}