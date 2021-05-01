package nl.hannahsten.texifyidea.run.ui.console.logtab.messagehandlers.warnings

import nl.hannahsten.texifyidea.run.ui.console.logtab.LatexLogMessageType
import nl.hannahsten.texifyidea.run.ui.console.logtab.LatexMessageWithLineHandler

/**
 * Line number occurs in a unique location:
 *
 * (\end occurred inside a group at level 1)
 * ### simple group (level 1) entered at line 4 ({)
 */
object EndOccurredInsideGroupWarningHandler : LatexMessageWithLineHandler(
    LatexLogMessageType.WARNING,
    """^\(\\end occurred inside a group at level .+ entered at line (\d+) .+\)""".toRegex()
)