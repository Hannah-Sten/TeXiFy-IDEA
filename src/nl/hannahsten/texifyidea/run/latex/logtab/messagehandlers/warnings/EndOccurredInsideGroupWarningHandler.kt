package nl.hannahsten.texifyidea.run.latex.logtab.messagehandlers.warnings

import nl.hannahsten.texifyidea.run.latex.logtab.LatexLogMessage
import nl.hannahsten.texifyidea.run.latex.logtab.LatexLogMessageType
import nl.hannahsten.texifyidea.run.latex.logtab.LatexMessageHandler

/**
 * Line number occurs in a unique location:
 *
 * (\end occurred inside a group at level 1)
 * ### simple group (level 1) entered at line 4 ({)
 */
object EndOccurredInsideGroupWarningHandler : LatexMessageHandler(
    LatexLogMessageType.WARNING,
    """^\(\\end occurred inside a group at level .+ entered at line (\d+) .+\)""".toRegex()
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