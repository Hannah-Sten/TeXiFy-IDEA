package nl.hannahsten.texifyidea.run.latex.ui

import com.intellij.execution.process.ProcessEvent
import com.intellij.execution.process.ProcessListener
import com.intellij.execution.process.ProcessOutputType
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Key
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.util.ui.MessageCategory
import nl.hannahsten.texifyidea.run.latex.ui.LatexOutputListener.LatexLogMessageType.ERROR
import nl.hannahsten.texifyidea.run.latex.ui.LatexOutputListener.LatexLogMessageType.WARNING
import nl.hannahsten.texifyidea.util.files.findFile
import org.apache.commons.collections.buffer.CircularFifoBuffer
import javax.swing.DefaultListModel

class LatexOutputListener(
        val project: Project,
        val mainFile: VirtualFile?,
        val listModel: DefaultListModel<String>,
        val treeView: LatexCompileMessageTreeView,
        val lineWidth: Int = 79
) : ProcessListener {

    /**
     * Window of the last two log output messages.
     */
    val window = CircularFifoBuffer(2)

    var isCollectingMessage = false
    var currentMessageText: String? = null

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
        window.add(newText)
        val text = window.joinToString(separator = "")

        // Check if we are currently in the process of collecting the full message of a matched message of interest
        if (isCollectingMessage) {
            collectMessageLine(newText)
        }
        else {
            // If now the first entry in window is an empty line: ignore this and skip to the next line
            if ((window.firstOrNull() as? String).isNullOrEmpty()) return

            val (message, fileName, line, type) =
                    findMessage(text, newText) ?: return

            val file = mainFile?.parent?.findFile(fileName ?: mainFile.name, setOf("tex"))

            if (message.length >= lineWidth) {
                // Keep on collecting output for this message
                currentMessageText = message
                isCollectingMessage = true
                collectMessageLine(newText)
            }
            else {
                if (listModel.isEmpty || listModel.lastElement() != message) {
                    addMessageToLog(message, file ?: mainFile, line
                            ?: 0, type.category)
                }
            }
        }
    }

    fun findMessage(text: String, newText: String): LatexLogMessage? {
        // Check if we have found an error
        ERROR_REGEX.find(text)?.apply {
            val line = groups["line"]?.value?.toInt()?.minus(1)
            val fileName = groups["file"]?.value?.trim()
            val message = groups["message"]?.value?.removeSuffix(newText) ?: ""
            return LatexLogMessage(message, fileName, line, ERROR)
        }

        // Check if we have found a warning
        if (TEX_WARNINGS.any { text.startsWith(it) }) {
            return LatexLogMessage(text.removeSuffix(newText), type = WARNING)
        }
        return null
    }

    private fun collectMessageLine(newText: String) {
        currentMessageText += newText

        if (newText.length < lineWidth) {
            isCollectingMessage = false
            addMessageToLog(currentMessageText!!)
        }
    }

    private fun addMessageToLog(message: String, file: VirtualFile? = mainFile, line: Int = 0, category: Int = MessageCategory.ERROR) {
        treeView.addMessage(category, arrayOf(message), file, line, 0, null)
        listModel.addElement(message)
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

    companion object {
        private val ERROR_REGEX = """^(?<file>.+)?:(?<line>\d+): (?<message>.+)$""".toRegex()
        private val TEX_WARNINGS = listOf(
                "LaTeX Warning: ",
                "LaTeX Font Warning: ",
                "AVAIL list clobbered at",
                "Double-AVAIL list clobbered at",
                "Doubly free location at",
                "Bad flag at",
                "Runaway definition",
                "Runaway argument",
                "Runaway text",
                "Missing character: There is no",
                "No pages of output.",
                "Underfull \\hbox",
                "Overfull \\hbox",
                "Loose \\hbox",
                "Tight \\hbox",
                "Underfull \\vbox",
                "Overfull \\vbox",
                "Loose \\vbox",
                "Tight \\vbox"
        )
    }

    data class LatexLogMessage(val message: String, val fileName: String? = null, val line: Int? = null, val type: LatexLogMessageType = ERROR)

    enum class LatexLogMessageType(val category: Int) {
        ERROR(MessageCategory.ERROR),
        PACKAGE_ERROR(MessageCategory.ERROR),
        WARNING(MessageCategory.WARNING),
        FONT_WARNING(MessageCategory.WARNING);
    }
}