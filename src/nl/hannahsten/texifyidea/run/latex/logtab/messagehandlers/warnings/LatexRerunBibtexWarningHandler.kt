package nl.hannahsten.texifyidea.run.latex.logtab.messagehandlers.warnings

import nl.hannahsten.texifyidea.run.latex.logtab.LatexLogMessage
import nl.hannahsten.texifyidea.run.latex.logtab.LatexLogMessageType.WARNING
import nl.hannahsten.texifyidea.run.latex.logtab.LatexMessageHandler
import nl.hannahsten.texifyidea.run.latex.logtab.LogMagicRegex.PACKAGE_REGEX

object LatexRerunBibtexWarningHandler : LatexMessageHandler(
        WARNING,
        """^Package $PACKAGE_REGEX Warning: (?<message>.+)$""".toRegex()
) {
    override fun findMessage(text: String, newText: String, currentFile: String?): LatexLogMessage? {
        LatexPackageWarningHandler.regex.first().find(text)?.apply {
            val `package` = groups["package"]?.value
            val unProcessedMessage = groups["message"]?.value ?: return@apply
            val message = """\(${`package`}\)\s+""".toRegex().replace(unProcessedMessage, " ")
            return LatexLogMessage("${`package`}: $message", fileName = currentFile, type = super.messageType)
        }
        return null
    }
}