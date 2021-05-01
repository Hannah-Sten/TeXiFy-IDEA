package nl.hannahsten.texifyidea.run.ui.console.logtab.messagehandlers.warnings

import nl.hannahsten.texifyidea.run.ui.console.logtab.LatexLogMessageType
import nl.hannahsten.texifyidea.run.ui.console.logtab.LatexMessageWithLineHandler

/**
 * Overfull hbox-like warnings, which have the line number at the end of the warning message in a special way specific to these TeX warnings only.
 */
object OverfullHboxWarningHandler : LatexMessageWithLineHandler(
    LatexLogMessageType.WARNING,
    """^(?:Loose \\hbox|Loose \\vbox|Overfull \\hbox|Overfull \\vbox|Tight \\hbox|Tight \\vbox|Underfull \\hbox|Underfull \\vbox) \(.+\) (?:detected at line (\d+)|has occurred while \\output is active|in alignment at lines (\d+)--\d+|in paragraph at lines (\d+)--\d+)""".toRegex()
)