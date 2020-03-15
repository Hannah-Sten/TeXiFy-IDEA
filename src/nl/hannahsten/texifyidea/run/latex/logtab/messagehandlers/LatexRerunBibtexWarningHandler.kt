package nl.hannahsten.texifyidea.run.latex.logtab.messagehandlers

import nl.hannahsten.texifyidea.run.latex.logtab.LatexLogMessage
import nl.hannahsten.texifyidea.run.latex.logtab.LatexLogMessageType
import nl.hannahsten.texifyidea.run.latex.logtab.LatexMessageHandler

object LatexRerunBibtexWarningHandler : LatexMessageHandler(
        LatexLogMessageType.WARNING,
        """^Package $PACKAGE_REGEX Warning: (?<message>.+)$""".toRegex()
) {
    override fun findMessage(text: String, newText: String, currentFile: String?): LatexLogMessage? {
        LatexPackageWarningHandler.regex.first().find(text)?.apply {
            val `package` = groups["package"]?.value
            val unProcessedMessage = groups["message"]?.value ?: return@apply
            val message = """\(${`package`}\)\s+""".toRegex().replace(unProcessedMessage, " ")
            return LatexLogMessage("${`package`}: $message", fileName = currentFile, type = LatexPackageWarningHandler.messageType)
        }
        return null
    }
}