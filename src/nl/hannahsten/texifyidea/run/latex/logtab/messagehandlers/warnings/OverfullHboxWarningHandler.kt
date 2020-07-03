package nl.hannahsten.texifyidea.run.latex.logtab.messagehandlers.warnings

import nl.hannahsten.texifyidea.run.latex.logtab.LatexLogMessage
import nl.hannahsten.texifyidea.run.latex.logtab.LatexLogMessageType
import nl.hannahsten.texifyidea.run.latex.logtab.LatexMessageHandler

/**
 * Overfull hbox-like warnings, which have the line number at the end of the warning message in a special way specific to these TeX warnings only.
 */
object OverfullHboxWarningHandler : LatexMessageHandler(
    LatexLogMessageType.WARNING,
    """^(?:Loose \\hbox|Loose \\vbox|Overfull \\hbox|Overfull \\vbox|Tight \\hbox|Tight \\vbox|Underfull \\hbox|Underfull \\vbox) \(.+\) (?:detected at line (\d+)|has occurred while \\output is active|in alignment at lines (\d+)--\d+|in paragraph at lines (\d+)--\d+)""".toRegex()
) {
    override fun findMessage(text: String, newText: String, currentFile: String?): LatexLogMessage? {
        regex.first().find(text)?.apply {
            val message = this.value
            val line = groupValues.drop(1).lastOrNull()?.toInt() ?: 1
            return LatexLogMessage(message, currentFile, line, messageType)
        }
        return null
    }
}