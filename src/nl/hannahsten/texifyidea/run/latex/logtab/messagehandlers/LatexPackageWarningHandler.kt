package nl.hannahsten.texifyidea.run.latex.logtab.messagehandlers

import nl.hannahsten.texifyidea.run.latex.logtab.LatexLogMessage
import nl.hannahsten.texifyidea.run.latex.logtab.LatexLogMessageType
import nl.hannahsten.texifyidea.run.latex.logtab.LatexMessageHandler

object LatexPackageWarningHandler : LatexMessageHandler(
        LatexLogMessageType.WARNING,
        """^Package $PACKAGE_REGEX Warning: (?<message>.+)$""".toRegex()
) {
    override fun findMessage(text: String, newText: String, currentFile: String?): LatexLogMessage? {
        regex.first().find(text)?.apply {
            val `package` = groups["package"]?.value
            val message = groups["message"]?.value?.trim()
            return LatexLogMessage("${`package`}: $message", fileName = currentFile, type = messageType)
        }
        return null
    }
}