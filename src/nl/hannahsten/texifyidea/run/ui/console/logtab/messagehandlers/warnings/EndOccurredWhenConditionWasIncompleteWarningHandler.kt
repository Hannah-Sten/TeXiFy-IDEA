package nl.hannahsten.texifyidea.run.ui.console.logtab.messagehandlers.warnings

import nl.hannahsten.texifyidea.run.ui.console.logtab.LatexLogMessageType
import nl.hannahsten.texifyidea.run.ui.console.logtab.LatexMessageWithLineHandler

/**
 * Similar to [EndOccurredInsideGroupWarningHandler].
 */
object EndOccurredWhenConditionWasIncompleteWarningHandler : LatexMessageWithLineHandler(
    LatexLogMessageType.WARNING,
    """^\(\\end occurred when .+ on line (\d+) was incomplete\)""".toRegex()
)