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
import org.apache.commons.collections.Buffer
import org.apache.commons.collections.BufferUtils
import org.apache.commons.collections.buffer.CircularFifoBuffer

class LatexOutputListener(
    val project: Project,
    val mainFile: VirtualFile?,
    val messageList: MutableList<LatexLogMessage>,
    val bibMessageList: MutableList<BibtexLogMessage>,
    val treeView: LatexCompileMessageTreeView,
    private val lineWidth: Int = LINE_WIDTH
) : ProcessListener {

    // This should probably be located somewhere else
    companion object {
        /**
         * Returns true if newText is most likely the last line of the message.
         */
        fun shouldStopCollectingMessage(newText: String): Boolean {
            return newText.length < LINE_WIDTH &&
                    // Indent of LaTeX Warning/Error messages
                    !newText.startsWith("               ") &&
                    // Package warning/error continuation.
                    !PACKAGE_WARNING_CONTINUATION.toRegex().containsMatchIn(newText) &&
                    LatexLogMagicRegex.TEX_MISC_WARNINGS_MULTIPLE_LINES.none { newText.startsWith(it) }
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
        val newText = event.text
        newText.chunked(lineWidth).forEach {
            processNewText(it)
        }
    }

    fun processNewText(newText: String) {
        if (isCollectingBib) {
            bibtexOutputListener.processNewText(newText)
        }
        else {
            processNewTextLatex(newText)
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
            collectMessageLine(newText)
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
            val logMessage = LatexLogMessageExtractor.findMessage(text, newText, fileStack.peek())

            // Check for potential file opens/closes, modify the stack accordingly.
            fileStack.update(newText)

            // Finally add the message to the log, or continue collecting this message when necessary.
            addOrCollectMessage(newText, logMessage ?: return)
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
    private fun addOrCollectMessage(
        newText: String,
        logMessage: LatexLogMessage
    ) {
        logMessage.apply {
            if (message.isEmpty()) return

            // Check length of the string we would append to the message, if it fills the line then we assume it continues on the next line
            if (!shouldStopCollectingMessage(newText)) {
                // Keep on collecting output for this message
                currentLogMessage = logMessage
                isCollectingMessage = true
                checkIfShouldStopCollectingMessage(newText)
            }
            else {
                val file = findProjectFileRelativeToMain(fileName)

                if (messageList.isEmpty() || !messageList.contains(logMessage)) {
                    // Use original filename, especially for tests to work (which cannot find the real file)
                    addMessageToLog(LatexLogMessage(message, fileName, line, type), file)
                }
            }
        }
    }

    private fun checkIfShouldStopCollectingMessage(newText: String) {
        if (shouldStopCollectingMessage(newText)) {
            isCollectingMessage = false
            addMessageToLog(currentLogMessage!!)
            currentLogMessage = null
        }
    }

    /**
     * Add the current/new message to the log if it does not continue on the
     * next line.
     */
    private fun collectMessageLine(newText: String, logMessage: LatexLogMessage? = null) {
        if (currentLogMessage?.message?.endsWith(newText.trim()) == false) {
            // Append new text
            val message = logMessage ?: currentLogMessage!!
            val newTextTrimmed = if (newText.length < lineWidth) " ${newText.trim()}" else newText.trim()
            // LaTeX Warning: is replaced here because this method is also run when a message is added,
            // and the above check needs to return false so we can't replace this in the WarningHandler
            val newMessage = (message.message + newTextTrimmed).replace("LaTeX Warning: ", "")
                .replace(PACKAGE_WARNING_CONTINUATION.toRegex(), "")
                .replace(DUPLICATE_WHITESPACE.toRegex(), "")
            currentLogMessage = LatexLogMessage(newMessage, message.fileName, message.line, message.type)
        }

        checkIfShouldStopCollectingMessage(newText)
    }

    private fun addMessageToLog(logMessage: LatexLogMessage, givenFile: VirtualFile? = null) {
        // Don't log the same message twice
        if (!messageList.contains(logMessage)) {
            val file = givenFile ?: findProjectFileRelativeToMain(logMessage.fileName)
            messageList.add(LatexLogMessage(logMessage.message.trim(), logMessage.fileName, logMessage.line, logMessage.type))
            // Correct the index because the treeview starts counting at line 0 instead of line 1.
            treeView.addMessage(
                logMessage.type.category,
                arrayOf(logMessage.message),
                file,
                logMessage.line - 1,
                -1,
                null
            )
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