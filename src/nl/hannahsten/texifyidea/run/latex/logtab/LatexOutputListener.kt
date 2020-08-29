package nl.hannahsten.texifyidea.run.latex.logtab

import com.intellij.execution.process.ProcessEvent
import com.intellij.execution.process.ProcessListener
import com.intellij.execution.process.ProcessOutputType
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Key
import com.intellij.openapi.vfs.VirtualFile
import nl.hannahsten.texifyidea.run.bibtex.logtab.BibtexLogMessage
import nl.hannahsten.texifyidea.run.bibtex.logtab.BibtexOutputListener
import nl.hannahsten.texifyidea.run.latex.logtab.LatexLogMagicRegex.DUPLICATE_WHITESPACE
import nl.hannahsten.texifyidea.run.latex.logtab.LatexLogMagicRegex.LINE_WIDTH
import nl.hannahsten.texifyidea.run.latex.logtab.LatexLogMagicRegex.PACKAGE_WARNING_CONTINUATION
import nl.hannahsten.texifyidea.run.latex.logtab.ui.LatexCompileMessageTreeView
import nl.hannahsten.texifyidea.util.files.findFile
import nl.hannahsten.texifyidea.util.remove
import nl.hannahsten.texifyidea.util.removeAll
import org.apache.commons.collections.Buffer
import org.apache.commons.collections.BufferUtils
import org.apache.commons.collections.buffer.CircularFifoBuffer

class LatexOutputListener(
    val project: Project,
    val mainFile: VirtualFile?,
    val messageList: MutableList<LatexLogMessage>,
    val bibMessageList: MutableList<BibtexLogMessage>,
    val treeView: LatexCompileMessageTreeView
) : ProcessListener {

    // This should probably be located somewhere else
    companion object {
        /**
         * Returns true if firstLine is most likely the last line of the message.
         */
        fun isLineEndOfMessage(secondLine: String, firstLine: String): Boolean {
            return firstLine.remove("\n").length < LINE_WIDTH - 1 &&
                    // Indent of LaTeX Warning/Error messages
                    !secondLine.startsWith("               ") &&
                    // Package warning/error continuation.
                    !PACKAGE_WARNING_CONTINUATION.toRegex().containsMatchIn(secondLine)
        }
    }

    /**
     * Window of the last two log output messages.
     */
    val window: Buffer = BufferUtils.synchronizedBuffer(CircularFifoBuffer(2))

    // For latexmk, collect the bibtex/biber messages in a separate list, so
    // we don't lose them when resetting on each new (pdfla)tex run.
    private var isCollectingBib = false
    private val bibtexOutputListener = BibtexOutputListener(project, mainFile, bibMessageList, treeView)

    var isCollectingMessage = false
    var currentLogMessage: LatexLogMessage? = null

    // Stack with the filenames, where the first is the current file.
    private var fileStack = LatexFileStack()

    override fun onTextAvailable(event: ProcessEvent, outputType: Key<*>) {
        if (outputType !is ProcessOutputType) return
        // Latexmk outputs on stderr, which interleaves with the pdflatex/bibtex/etc output on stdout
        if (outputType.isStderr) return

        // We look for specific strings in the log output for which we know it was probably an error message
        // (it might be that it is actually not, since anything can be typed to the LaTeX output log, but we ignore
        // that for now and assume the log is of reasonable format).
        // Standard log output is only 79 characters wide, meaning these strings might occur over two lines.
        // We assume the match tokens will always fit over two lines.
        // Therefore, we maintain a window of two log output lines to detect the error messages.
        //
        // We assume that if the first line of the error/warning is exactly the maximum line width, it will continue
        // on the next line. This may not be accurate, but there is no way of distinguishing this.

        // Newlines are important to check when message end. Keep.
        processNewText(event.text)
    }

    fun processNewText(newText: String) {
        if (isCollectingBib) {
            // Don't chunk bibtex lines, as they don't usually break by themselves and they are often not long anyway
            bibtexOutputListener.processNewText(newText)
        }
        else {
            newText.chunked(LINE_WIDTH).forEach {
                processNewTextLatex(it)
            }
        }
        resetIfNeeded(newText)
    }

    private fun processNewTextLatex(newText: String) {
        window.add(newText)
        // No idea how we could possibly get an IndexOutOfBoundsException on the buffer, but we did
        val text = try {
            window.joinToString(separator = "")
        }
        catch (e: IndexOutOfBoundsException) {
            return
        }

        // Check if we are currently in the process of collecting the full message of a matched message of interest
        if (isCollectingMessage) {
            collectMessageLine(text, newText)
            fileStack.update(newText)
        }
        else {
            // Skip line if it is irrelevant.
            if (LatexLogMessageExtractor.skip(window.firstOrNull() as? String)) {
                // The first line might be irrelevant, but the new text could
                // contain useful information about the file stack.
                fileStack.update(newText)
                return
            }

            // Find an error message or warning in the current text.
            val logMessage = LatexLogMessageExtractor.findMessage(text.removeAll("\n", "\r"), newText.removeAll("\n"), fileStack.peek())

            // Check for potential file opens/closes, modify the stack accordingly.
            fileStack.update(newText)

            // Finally add the message to the log, or continue collecting this message when necessary.
            addOrCollectMessage(text, newText, logMessage ?: return)
        }
    }

    private fun findProjectFileRelativeToMain(fileName: String?): VirtualFile? =
        mainFile?.parent?.findFile(fileName ?: mainFile.name, setOf("tex"))

    /**
     * Reset the tree view and the message list when starting a new run. (latexmk)
     */
    private fun resetIfNeeded(newText: String) {
        """Latexmk: applying rule '(?<program>\w+)""".toRegex().apply {
            val result = find(newText) ?: return@apply
            if (result.groups["program"]?.value in setOf("biber", "bibtex")) {
                isCollectingBib = true
            }
            else {
                isCollectingBib = false
                treeView.errorViewStructure.clear()
                messageList.clear()
                // Re-add the bib messages to the tree.
                bibMessageList.forEach { bibtexOutputListener.addBibMessageToTree(it) }
            }
        }
    }

    /**
     * Keep collecting the message if necessary, otherwise add it to the log.
     */
    private fun addOrCollectMessage(text: String, newText: String, logMessage: LatexLogMessage) {
        logMessage.apply {
            if (message.isEmpty()) return

            if (!isLineEndOfMessage(newText, logMessage.message) || text.removeSuffix(newText).length >= LINE_WIDTH) {
                // Keep on collecting output for this message
                currentLogMessage = logMessage
                isCollectingMessage = true
            }
            else {
                val file = findProjectFileRelativeToMain(fileName)

                if (messageList.isEmpty() || !messageList.contains(logMessage)) {
                    // Use original filename, especially for tests to work (which cannot find the real file)
                    // Trim message here instead of earlier in order to keep spaces in case we needed to continue
                    // collecting the message and the spaces were actually relevant
                    addMessageToLog(LatexLogMessage(message.trim(), fileName, line, type), file)
                }
            }
        }
    }

    /**
     * Add the current/new message to the log if it does not continue on the
     * next line.
     */
    private fun collectMessageLine(text: String, newText: String, logMessage: LatexLogMessage? = null) {
        // Check if newText is interesting before appending it to the message
        if (currentLogMessage?.message?.endsWith(newText.trim()) == false && !isLineEndOfMessage(newText, text.removeSuffix(newText))) {
            // Append new text
            val message = logMessage ?: currentLogMessage!!

            // Assume that lines that end prematurely do need an extra space to be inserted, like LaTeX and package
            // warnings with manual newlines, unlike 80-char forced linebreaks which should not have a space inserted
            val newTextTrimmed = if (text.removeSuffix(newText).length < LINE_WIDTH) " ${newText.trim()}" else newText.trim()

            // LaTeX Warning: is replaced here because this method is also run when a message is added,
            // and the above check needs to return false so we can't replace this in the WarningHandler
            var newMessage = (message.message + newTextTrimmed).replace("LaTeX Warning: ", "")
                .replace(PACKAGE_WARNING_CONTINUATION.toRegex(), "")
                .replace(DUPLICATE_WHITESPACE.toRegex(), " ")

            // The 'on input line <line>' may be a lot of lines after the 'LaTeX Warning:', thus the original regex may
            // not have caught it. Try to catch the line number here.
            var line = message.line
            if (line == -1) {
                LatexLogMagicRegex.REPORTED_ON_LINE_REGEX.find(newMessage)?.apply {
                    line = groups["line"]?.value?.toInt() ?: -1
                    newMessage = newMessage.removeAll(this.value).trim()
                }
            }

            currentLogMessage = LatexLogMessage(newMessage, message.fileName, line, message.type)
        }
        else {
            isCollectingMessage = false
            addMessageToLog(currentLogMessage!!)
            currentLogMessage = null
        }
    }

    private fun addMessageToLog(logMessage: LatexLogMessage, givenFile: VirtualFile? = null) {
        // Don't log the same message twice
        if (!messageList.contains(logMessage)) {
            val file = givenFile ?: findProjectFileRelativeToMain(logMessage.fileName)
            val message = LatexLogMessage(logMessage.message.trim(), logMessage.fileName, logMessage.line, logMessage.type, file)
            messageList.add(message)
            treeView.applyFilters(message)
        }
    }

    override fun processTerminated(event: ProcessEvent) {
        if (event.exitCode == 0) {
            treeView.setProgressText("Compilation was successful.")
        }
        else {
            treeView.setProgressText("Compilation failed.")
        }
    }

    override fun processWillTerminate(event: ProcessEvent, willBeDestroyed: Boolean) {
    }

    override fun startNotified(event: ProcessEvent) {
        treeView.setProgressText("Compilation in progress...")
    }
}