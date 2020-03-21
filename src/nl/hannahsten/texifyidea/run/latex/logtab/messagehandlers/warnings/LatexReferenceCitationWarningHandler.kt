package nl.hannahsten.texifyidea.run.latex.logtab.messagehandlers.warnings

import nl.hannahsten.texifyidea.run.latex.logtab.LatexLogMessage
import nl.hannahsten.texifyidea.run.latex.logtab.LatexLogMessageType
import nl.hannahsten.texifyidea.run.latex.logtab.LatexMessageHandler
import nl.hannahsten.texifyidea.run.latex.logtab.LogMagicRegex.LATEX_WARNING_REGEX
import nl.hannahsten.texifyidea.run.latex.logtab.LogMagicRegex.LINE_REGEX
import nl.hannahsten.texifyidea.run.latex.logtab.LogMagicRegex.REFERENCE_REGEX

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
            return LatexLogMessage(message, currentFile, line, messageType)
        }
        return null
    }
}