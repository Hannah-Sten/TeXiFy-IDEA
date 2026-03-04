package nl.hannahsten.texifyidea.run.latex.steplog

import com.intellij.openapi.vfs.VirtualFile
import nl.hannahsten.texifyidea.run.bibtex.logtab.BibtexLogMagicRegex
import nl.hannahsten.texifyidea.run.bibtex.logtab.BibtexLogMessage
import nl.hannahsten.texifyidea.run.bibtex.logtab.BibtexLogMessageType
import nl.hannahsten.texifyidea.run.bibtex.logtab.messagehandlers.errors.AuxEndErrBibtexMessageHandler
import nl.hannahsten.texifyidea.run.bibtex.logtab.messagehandlers.errors.AuxErrPrintBibtexMessageHandler
import nl.hannahsten.texifyidea.run.bibtex.logtab.messagehandlers.errors.BiberErrorBibtexSubsystemMessageHandler
import nl.hannahsten.texifyidea.run.bibtex.logtab.messagehandlers.errors.BiberErrorMessageHandler
import nl.hannahsten.texifyidea.run.bibtex.logtab.messagehandlers.errors.BstExWarnPrintBibtexMessageHandler
import nl.hannahsten.texifyidea.run.bibtex.logtab.messagehandlers.errors.CleanUpAndLeaveBibtexMessageHandler
import nl.hannahsten.texifyidea.run.bibtex.logtab.messagehandlers.errors.NonexistentCrossReferenceBibtexMessageHandler
import nl.hannahsten.texifyidea.run.bibtex.logtab.messagehandlers.warnings.BibLnNumPrintBibtexMessageHandler
import nl.hannahsten.texifyidea.run.bibtex.logtab.messagehandlers.warnings.BiberWarningBibtexSubsystemMessageHandler
import nl.hannahsten.texifyidea.run.bibtex.logtab.messagehandlers.warnings.BiberWarningMessageHandler
import nl.hannahsten.texifyidea.run.bibtex.logtab.messagehandlers.warnings.NoFieldsBibtexMessageHandler
import nl.hannahsten.texifyidea.run.bibtex.logtab.messagehandlers.warnings.WarningBibtexMessageHandler
import nl.hannahsten.texifyidea.util.files.findFile

internal class BibtexStepMessageParserSession(
    private val mainFile: VirtualFile?,
) : StepMessageParserSession {

    override val supportsStructuredMessages: Boolean = true

    private val window = ArrayDeque<String>()
    private val emittedMessages = linkedSetOf<BibtexLogMessage>()
    private var currentFile: String = ""
    private var collectingOutputLine: String = ""

    override fun onText(text: String): List<ParsedStepMessage> {
        if (text.isEmpty()) {
            return emptyList()
        }

        val merged = collectingOutputLine + text
        val lines = merged.split('\n')
        val completeLines = if (merged.endsWith("\n")) {
            collectingOutputLine = ""
            lines.dropLast(1)
        }
        else {
            collectingOutputLine = lines.last()
            lines.dropLast(1)
        }

        val result = mutableListOf<ParsedStepMessage>()
        completeLines.forEach { line ->
            result += processLine(line)
        }
        return result
    }

    private fun processLine(newText: String): List<ParsedStepMessage> {
        window += newText
        while (window.size > 5) {
            window.removeFirst()
        }

        updateCurrentFile(newText)
        val logMessage = extractMessage(window.toList()) ?: return emptyList()
        if (!emittedMessages.add(logMessage)) {
            return emptyList()
        }

        val file = mainFile?.parent?.findFile(logMessage.fileName ?: mainFile.name, supportsAnyExtension = true)
        val withFile = logMessage.copy(file = file)
        val level = when (withFile.type) {
            BibtexLogMessageType.ERROR -> ParsedStepMessageLevel.ERROR
            BibtexLogMessageType.WARNING -> ParsedStepMessageLevel.WARNING
        }
        return listOf(
            ParsedStepMessage(
                message = withFile.message,
                level = level,
                fileName = withFile.fileName,
                line = withFile.line,
                file = withFile.file,
            )
        )
    }

    private fun extractMessage(windowList: List<String>): BibtexLogMessage? {
        val errorHandlers = listOf(
            BstExWarnPrintBibtexMessageHandler,
            AuxErrPrintBibtexMessageHandler,
            CleanUpAndLeaveBibtexMessageHandler,
            AuxEndErrBibtexMessageHandler,
            NonexistentCrossReferenceBibtexMessageHandler,
            BiberErrorBibtexSubsystemMessageHandler,
            BiberErrorMessageHandler,
        )
        val warningHandlers = listOf(
            NoFieldsBibtexMessageHandler,
            WarningBibtexMessageHandler,
            BibLnNumPrintBibtexMessageHandler,
            BiberWarningBibtexSubsystemMessageHandler,
            BiberWarningMessageHandler,
        )

        (errorHandlers + warningHandlers).forEach { handler ->
            handler.findMessage(windowList, currentFile).let { message ->
                if (message != null) {
                    return message
                }
            }
        }

        return null
    }

    private fun updateCurrentFile(newText: String) {
        BibtexLogMagicRegex.bibFileOpened.find(newText)?.apply {
            currentFile = groups["file"]?.value ?: ""
        }
        BibtexLogMagicRegex.biberFileOpened.find(newText)?.apply {
            currentFile = groups["file"]?.value ?: ""
        }
    }
}
