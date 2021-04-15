package nl.hannahsten.texifyidea.run.ui.console.logtab.messagehandlers.warnings

import nl.hannahsten.texifyidea.run.ui.console.logtab.LatexLogMagicRegex.LATEX_WARNING_REGEX
import nl.hannahsten.texifyidea.run.ui.console.logtab.LatexLogMagicRegex.LINE_REGEX
import nl.hannahsten.texifyidea.run.ui.console.logtab.LatexLogMagicRegex.REFERENCE_REGEX
import nl.hannahsten.texifyidea.run.ui.console.logtab.LatexLogMessage
import nl.hannahsten.texifyidea.run.ui.console.logtab.LatexLogMessageType
import nl.hannahsten.texifyidea.run.ui.console.logtab.LatexMessageHandler

/**
 * Reference `<key>' on page <number> undefined (p927 LaTeX Companion).
 */
object LatexReferenceCitationWarningHandler : LatexMessageHandler(
    LatexLogMessageType.WARNING,
    """^$LATEX_WARNING_REGEX (?<ref>Reference|Citation) $REFERENCE_REGEX on page \d+ undefined $LINE_REGEX$""".toRegex()
) {

    override fun findMessage(text: String, newText: String, currentFile: String?): LatexLogMessage? {
        regex.first().find(text)?.apply {
            val ref = groups["ref"]?.value
            val label = groups["label"]?.value
            val message = "$ref $label undefined"
            val line = groups["line"]?.value?.toInt()
            return LatexLogMessage(message, currentFile, line ?: 1, messageType)
        }
        return null
    }
}