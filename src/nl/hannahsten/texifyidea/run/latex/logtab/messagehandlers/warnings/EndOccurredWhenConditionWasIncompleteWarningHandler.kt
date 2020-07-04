package nl.hannahsten.texifyidea.run.latex.logtab.messagehandlers.warnings

import nl.hannahsten.texifyidea.run.latex.logtab.LatexLogMessageType
import nl.hannahsten.texifyidea.run.latex.logtab.LatexMessageWithLineHandler

/**
 * Similar to [EndOccurredInsideGroupWarningHandler].
 */
object EndOccurredWhenConditionWasIncompleteWarningHandler : LatexMessageWithLineHandler(
    LatexLogMessageType.WARNING,
    """^\(\\end occurred when .+ on line (\d+) was incomplete\)""".toRegex()
)