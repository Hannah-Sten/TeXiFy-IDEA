package nl.hannahsten.texifyidea.run.latex.logtab.messagehandlers.warnings

import nl.hannahsten.texifyidea.run.latex.logtab.LatexLogMagicRegex.LINE_REGEX
import nl.hannahsten.texifyidea.run.latex.logtab.LatexLogMagicRegex.PACKAGE_REGEX
import nl.hannahsten.texifyidea.run.latex.logtab.LatexLogMessage
import nl.hannahsten.texifyidea.run.latex.logtab.LatexLogMessageType.WARNING
import nl.hannahsten.texifyidea.run.latex.logtab.LatexMessageHandler

object LatexPackageWarningHandler : LatexMessageHandler(
    WARNING,
    """^Package $PACKAGE_REGEX Warning: (?<message>.+) $LINE_REGEX$""".toRegex(),
    """^Package $PACKAGE_REGEX Warning: (?<message>.+)$""".toRegex()
) {

    override fun findMessage(text: String, newText: String, currentFile: String?): LatexLogMessage? {
        regex.forEach {
            it.find(text)?.apply {
                @Suppress("ktlint:standard:property-naming")
                val `package` = groups["package"]?.value
                val unProcessedMessage = groups["message"]?.value ?: return@apply
                // Remove the (package) occurrences in the rest of the warning message.
                val message =
                    """\(${`package`}\)\s+""".toRegex()
                        .replace(unProcessedMessage, " ").trim()
                val line = try {
                    groups["line"]?.value?.toInt() ?: -1
                }
                // This is the way to check for presence of regex groups apparently
                catch (ignored: IllegalArgumentException) {
                    -1
                }
                return LatexLogMessage("${`package`}: $message", fileName = currentFile, type = super.messageType, line = line)
            }
        }
        return null
    }
}