package nl.hannahsten.texifyidea.run.latex.logtab.messagehandlers.warnings

import nl.hannahsten.texifyidea.run.latex.logtab.LatexLogMessage
import nl.hannahsten.texifyidea.run.latex.logtab.LatexLogMessageType.WARNING
import nl.hannahsten.texifyidea.run.latex.logtab.LatexMessageHandler
import nl.hannahsten.texifyidea.run.latex.logtab.LatexLogMagicRegex.PACKAGE_REGEX

object LatexPackageWarningHandler : LatexMessageHandler(
        WARNING,
        """^Package $PACKAGE_REGEX Warning: (?<message>.+)$""".toRegex()
) {
    override fun findMessage(text: String, newText: String, currentFile: String?): LatexLogMessage? {
        regex.forEach {
            it.find(text)?.apply {
                val `package` = groups["package"]?.value
                val unProcessedMessage = groups["message"]?.value ?: return@apply
                // Remove the (package) occurrences in the rest of the warning message.
                val message = """\(${`package`}\)\s+""".toRegex()
                        .replace(unProcessedMessage, " ").trim()
                return LatexLogMessage("${`package`}: $message", fileName = currentFile, type = super.messageType)
            }
        }
        return null
    }
}