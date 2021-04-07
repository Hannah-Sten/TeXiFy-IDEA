package nl.hannahsten.texifyidea.run.bibtex.logtab.messagehandlers.warnings

import nl.hannahsten.texifyidea.run.bibtex.logtab.BibtexLogMagicRegex
import nl.hannahsten.texifyidea.run.bibtex.logtab.BibtexLogMessage
import nl.hannahsten.texifyidea.run.bibtex.logtab.BibtexLogMessageType
import nl.hannahsten.texifyidea.run.bibtex.logtab.BibtexMessageHandler

/**
 * Handle log messages that start with 'Warning--', of which there are a couple of versions.
 * Since --line may still appear on the next line, we catch this message in the middle of the buffer.
 */
object WarningBibtexMessageHandler : BibtexMessageHandler() {

    override fun findMessage(window: List<String>, currentFile: String): BibtexLogMessage? {
        if (window.size < 3) return null
        // Index of the middle element of the buffer (might not be fully filled)
        // This does assume that there will be at least two more lines after the last relevant line (generally true, with Process finished message)
        val middleIndex = window.size - 3

        // Handled in NoFieldsBibtexMessageHandler
        if (BibtexLogMagicRegex.bibLnNumPrint.containsMatchIn(window[middleIndex])) return null

        BibtexLogMagicRegex.warning.find(window[middleIndex])?.apply {
            var file = currentFile
            var line: Int? = null
            // Match instead of find in order to avoid matching 'while executing--.+', as we assume in that case that the error is in the bib, not in the bst
            BibtexLogMagicRegex.bibLnNumPrint.matchEntire(window[middleIndex + 1])?.apply line@{
                line = groups["line"]?.value?.toInt()
                file = groups["file"]?.value ?: return@line
            }

            // Handle a message which spans two lines for some reason (line 6633 in bibtex.web)
            val message = if (BibtexLogMagicRegex.nestedCrossReference.matches(window[middleIndex])) {
                groups["message"]?.value + " " + window[middleIndex + 1]
            }
            else {
                groups["message"]?.value
            }

            return BibtexLogMessage(message ?: "", file, line, BibtexLogMessageType.WARNING)
        }
        return null
    }
}