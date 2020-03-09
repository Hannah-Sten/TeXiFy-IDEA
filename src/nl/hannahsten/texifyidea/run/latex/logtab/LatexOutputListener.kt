package nl.hannahsten.texifyidea.run.latex.logtab

import com.intellij.execution.process.ProcessEvent
import com.intellij.execution.process.ProcessListener
import com.intellij.execution.process.ProcessOutputType
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Key
import com.intellij.openapi.vfs.VirtualFile
import nl.hannahsten.texifyidea.run.latex.logtab.LatexLogMessageType.ERROR
import nl.hannahsten.texifyidea.run.latex.ui.LatexCompileMessageTreeView
import nl.hannahsten.texifyidea.util.files.findFile
import org.apache.commons.collections.buffer.CircularFifoBuffer

class LatexOutputListener(
        val project: Project,
        val mainFile: VirtualFile?,
        val messageList: MutableList<LatexLogMessage>,
        val treeView: LatexCompileMessageTreeView,
        val lineWidth: Int = 79
) : ProcessListener {

    /**
     * Window of the last two log output messages.
     */
    val window = CircularFifoBuffer(2)

    var isCollectingMessage = false
    var currentMessageText: String? = null
    // Stack with the filenames, where the first is the current file.
    var fileStack = LatexFileStack()

    override fun onTextAvailable(event: ProcessEvent, outputType: Key<*>) {
        if (outputType !is ProcessOutputType) return

        // We look for specific strings in the log output for which we know it was probably an error message
        // (it might be that it is actually not, since anything can be typed to the LaTeX output log, but we ignore
        // that for now and assume the log is of reasonable format).
        // Standard log output is only 79 characters wide, meaning these strings might occur over two lines.
        // We assume the match tokens will always fit over two lines.
        // Therefore, we maintain a window of two log output lines to detect the error messages.
        //
        // We assume that if the first line of the error/warning is exactly the maximum line width, it will continue
        // on the next line. This may not be accurate, but there is no way of distinguishing this.

        val newText = event.text.trimEnd('\n', '\r')
        processNewText(newText)
    }

    fun processNewText(newText: String) {
        window.add(newText)
        val text = window.joinToString(separator = "")

        // Check if we are currently in the process of collecting the full message of a matched message of interest
        if (isCollectingMessage) {
            collectMessageLine(newText)
        }
        else {
            // If now the first entry in window is an empty line: ignore this and skip to the next line
            if ((window.firstOrNull() as? String).isNullOrEmpty()) return

            // Find an error message or warning in the current text.
            val logMessage = LatexLogMessageExtractor(text, newText, fileStack.peek()).findMessage() ?: return

            // TODO check for potential file opens/closes, modify the stack accordingly
            fileStack.update(text)

            // Finally add the message to the log, or continue collecting this message when necessary.
            addOrCollectMessage(newText, logMessage)
        }
    }

    private fun findProjectFileRelativeToMain(fileName: String?): VirtualFile? =
            mainFile?.parent?.findFile(fileName ?: mainFile.name, setOf("tex"))

    /**
     * Keep collecting the message if necessary, otherwise add it to the log.
     */
    private fun addOrCollectMessage(newText: String, logMessage: LatexLogMessage) {
        logMessage.apply {
            if (message.length >= lineWidth) {
                // Keep on collecting output for this message
                currentMessageText = message
                isCollectingMessage = true
                collectMessageLine(newText)
            }
            else {
                val file = findProjectFileRelativeToMain(fileName)

                if (messageList.isEmpty() || messageList.last().message != message) {
                    addMessageToLog(message, file ?: mainFile, line
                            ?: 0, type)
                }
            }
        }
    }

    /**
     * Add the current/new message to the log if it does not continue on the
     * next line.
     */
    private fun collectMessageLine(newText: String) {
        currentMessageText += newText

        if (newText.length < lineWidth) {
            isCollectingMessage = false
            addMessageToLog(currentMessageText!!)
        }
    }

    private fun addMessageToLog(message: String, file: VirtualFile? = mainFile, line: Int = 0, type: LatexLogMessageType = ERROR) {
        treeView.addMessage(type.category, arrayOf(message), file, line, 0, null)
        messageList.add(LatexLogMessage(message, file?.name, line, type))
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