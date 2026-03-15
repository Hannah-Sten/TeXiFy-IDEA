package nl.hannahsten.texifyidea.run.latex.steplog

import com.intellij.openapi.vfs.VirtualFile
import nl.hannahsten.texifyidea.run.latex.logtab.LatexFileStack
import nl.hannahsten.texifyidea.run.latex.logtab.LatexLogMagicRegex
import nl.hannahsten.texifyidea.run.latex.logtab.LatexLogMessage
import nl.hannahsten.texifyidea.run.latex.logtab.LatexLogMessageExtractor
import nl.hannahsten.texifyidea.run.latex.logtab.LatexLogMessageType
import nl.hannahsten.texifyidea.run.latex.logtab.LatexOutputListener
import nl.hannahsten.texifyidea.util.files.findFile
import nl.hannahsten.texifyidea.util.remove
import nl.hannahsten.texifyidea.util.removeAll

internal class LatexStepMessageParserSession(
    private val mainFile: VirtualFile?,
) : StepMessageParserSession {

    private companion object {

        val latexmkAppliedRuleRegex = Regex("""Latexmk: applying rule '(?<program>\w+)""", RegexOption.IGNORE_CASE)
        val latexmkRunNumberRegex = Regex("""Run number\s+(?<run>\d+)\s+of rule '(?<engine>(pdf|xe|lua)?latex)'""", RegexOption.IGNORE_CASE)
    }

    override val supportsStructuredMessages: Boolean = true

    private val emittedMessages = linkedSetOf<LatexLogMessage>()
    private val window = ArrayDeque<String>()

    private var isCollectingMessage = false
    private var currentLogMessage: LatexLogMessage? = null
    private var fileStack = LatexFileStack()
    private var collectingOutputLine: String = ""
    private var isCollectingBib = false
    private var bibtexParser = BibtexStepMessageParserSession(mainFile)

    override fun onText(text: String): List<ParsedStepEvent> {
        if (text.isEmpty()) {
            return emptyList()
        }

        if (!text.endsWith("\n")) {
            collectingOutputLine += text
            return emptyList()
        }

        val collectedLine = collectingOutputLine + text
        collectingOutputLine = ""

        val parsedMessages = mutableListOf<ParsedStepEvent>()
        if (isCollectingBib) {
            parsedMessages += bibtexParser.onText(collectedLine)
        }
        else {
            collectedLine.chunked(LatexLogMagicRegex.LINE_WIDTH).forEach { chunk ->
                parsedMessages += processChunk(chunk)
            }
        }
        updateLatexmkMode(collectedLine)
        return parsedMessages
    }

    private fun processChunk(newText: String): List<ParsedStepEvent> {
        val output = mutableListOf<ParsedStepEvent>()
        latexmkResetEvent(newText)?.let {
            resetLatexmkPassState()
            output += it
            return output
        }
        window += newText
        while (window.size > 2) {
            window.removeFirst()
        }
        val text = window.joinToString(separator = "")

        if (isCollectingMessage) {
            output += collectMessageLine(text, newText)
            fileStack.update(newText)
            return output
        }

        val firstLine = window.firstOrNull()
        if (LatexLogMessageExtractor.skip(firstLine)) {
            fileStack.update(newText)
            return output
        }

        val normalizedText = text.removeAll("\n", "\r")
        val normalizedNewText = newText.removeAll("\n")
        val message = LatexLogMessageExtractor.findMessage(normalizedText, normalizedNewText, fileStack.peek())
        fileStack.update(newText)
        if (message != null) {
            output += addOrCollectMessage(text, newText, message)
        }

        return output
    }

    private fun addOrCollectMessage(text: String, newText: String, logMessage: LatexLogMessage): List<ParsedStepEvent> {
        if (logMessage.message.isEmpty()) {
            return emptyList()
        }
        if (!LatexOutputListener.isLineEndOfMessage(newText, logMessage.message) || text.removeSuffix(newText).length >= LatexLogMagicRegex.LINE_WIDTH) {
            currentLogMessage = logMessage
            isCollectingMessage = true
            return emptyList()
        }

        val file = findProjectFileRelativeToMain(logMessage.fileName)
        val finalized = LatexLogMessage(logMessage.message.trim(), logMessage.fileName, logMessage.line, logMessage.type, file)
        return emitIfNew(finalized)
    }

    private fun collectMessageLine(text: String, newText: String): List<ParsedStepEvent> {
        val current = currentLogMessage ?: return emptyList()

        if (current.message.endsWith(newText.trim()).not() && !LatexOutputListener.isLineEndOfMessage(newText, text.removeSuffix(newText))) {
            val newTextTrimmed = if (text.removeSuffix(newText).length < LatexLogMagicRegex.LINE_WIDTH) {
                " ${newText.trim()}"
            }
            else {
                newText.trim()
            }

            var newMessage = (current.message + newTextTrimmed).replace("LaTeX Warning: ", "")
                .replace(LatexLogMagicRegex.PACKAGE_WARNING_CONTINUATION.toRegex(), "")
                .replace(LatexLogMagicRegex.DUPLICATE_WHITESPACE.toRegex(), " ")
                .replace(""". l.\d+ """.toRegex(), " ")

            var line = current.line
            if (line == -1) {
                LatexLogMagicRegex.REPORTED_ON_LINE_REGEX.find(newMessage)?.apply {
                    line = groups["line"]?.value?.toInt() ?: -1
                    newMessage = newMessage.remove(this.value).trim()
                }
            }

            currentLogMessage = LatexLogMessage(newMessage, current.fileName, line, current.type)
            return emptyList()
        }

        isCollectingMessage = false
        currentLogMessage = null
        val file = findProjectFileRelativeToMain(current.fileName)
        val finalized = LatexLogMessage(current.message.trim(), current.fileName, current.line, current.type, file)
        return emitIfNew(finalized)
    }

    private fun emitIfNew(message: LatexLogMessage): List<ParsedStepEvent> {
        if (!emittedMessages.add(message)) {
            return emptyList()
        }

        val level = when (message.type) {
            LatexLogMessageType.ERROR -> ParsedStepMessageLevel.ERROR
            LatexLogMessageType.WARNING -> ParsedStepMessageLevel.WARNING
        }
        return listOf(
            ParsedStepEvent.Message(
                ParsedStepMessage(
                    message = message.message,
                    level = level,
                    fileName = message.fileName,
                    line = message.line.takeIf { it >= 0 },
                    file = message.file,
                    source = ParsedStepMessageSource.LATEX,
                )
            )
        )
    }

    private fun latexmkResetEvent(newText: String): ParsedStepEvent? {
        val runNumber = latexmkRunNumberRegex.find(newText.trim())
            ?.groups
            ?.get("run")
            ?.value
            ?.toIntOrNull()
            ?: return null
        return if (runNumber > 1) ParsedStepEvent.ResetLatexMessages else null
    }

    private fun resetLatexmkPassState() {
        emittedMessages.clear()
        window.clear()
        isCollectingMessage = false
        currentLogMessage = null
        fileStack = LatexFileStack()
        collectingOutputLine = ""
    }

    private fun updateLatexmkMode(newText: String) {
        val program = latexmkAppliedRuleRegex.find(newText.trim())
            ?.groups
            ?.get("program")
            ?.value
            ?.lowercase()
            ?: return

        if (program in setOf("biber", "bibtex")) {
            isCollectingBib = true
            bibtexParser = BibtexStepMessageParserSession(mainFile)
        }
        else {
            isCollectingBib = false
        }
    }

    private fun findProjectFileRelativeToMain(fileName: String?): VirtualFile? =
        mainFile?.parent?.findFile(fileName ?: mainFile.name, listOf("tex"), supportsAnyExtension = true)
}
