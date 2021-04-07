package nl.hannahsten.texifyidea.run.latex.logtab.messagehandlers.warnings

import nl.hannahsten.texifyidea.run.latex.logtab.LatexLogMagicRegex
import nl.hannahsten.texifyidea.run.latex.logtab.LatexLogMessageType
import nl.hannahsten.texifyidea.run.latex.logtab.LatexMessageWithLineHandler

/**
 * Multi-line warning with line number.

LaTeX Warning: You have requested, on input line 5, version
`9999/99/99' of package test998,
but only version
`2020/04/08'
is available.

 */
object YouHaveRequestedOnInputLineVersionOfPackageWarningHandler : LatexMessageWithLineHandler(
    LatexLogMessageType.WARNING,
    """${LatexLogMagicRegex.LATEX_WARNING_REGEX} You have requested, on input line (\d+), .+""".toRegex()
)