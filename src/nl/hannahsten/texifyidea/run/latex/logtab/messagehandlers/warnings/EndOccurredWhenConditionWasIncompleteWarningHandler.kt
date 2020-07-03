package nl.hannahsten.texifyidea.run.latex.logtab.messagehandlers.warnings

import nl.hannahsten.texifyidea.run.latex.logtab.LatexLogMessage
import nl.hannahsten.texifyidea.run.latex.logtab.LatexLogMessageType
import nl.hannahsten.texifyidea.run.latex.logtab.LatexMessageHandler

/**
 * Similar to [EndOccurredInsideGroupWarningHandler].
 */
object EndOccurredWhenConditionWasIncompleteWarningHandler : LatexMessageHandler(
    LatexLogMessageType.WARNING,
    """^\(\\end occurred when .+ on line (\d+) was incomplete\)""".toRegex()
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