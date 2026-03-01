package nl.hannahsten.texifyidea.run.latex.step

import com.intellij.execution.process.ProcessHandler
import com.intellij.execution.process.ProcessTerminatedListener
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.util.execution.ParametersListUtil
import nl.hannahsten.texifyidea.run.OpenCustomPdfViewerListener
import nl.hannahsten.texifyidea.run.latex.PdfViewerStepOptions
import nl.hannahsten.texifyidea.run.pdfviewer.PdfViewer
import nl.hannahsten.texifyidea.run.pdfviewer.OpenViewerListener
import nl.hannahsten.texifyidea.util.caretOffset
import nl.hannahsten.texifyidea.util.focusedTextEditor
import nl.hannahsten.texifyidea.util.selectedTextEditor
import java.io.OutputStream

internal class PdfViewerRunStep(
    private val stepConfig: PdfViewerStepOptions,
) : ProcessLatexRunStep {

    override val configId: String = stepConfig.id
    override val id: String = stepConfig.type

    override fun createProcess(context: LatexRunStepContext): ProcessHandler {
        val process = object : ProcessHandler() {
            override fun destroyProcessImpl() {
                notifyProcessTerminated(0)
            }

            override fun detachProcessImpl() {
                notifyProcessDetached()
            }

            override fun detachIsDefault(): Boolean = false

            override fun getProcessInput(): OutputStream? = null

            override fun startNotify() {
                super.startNotify()
                scheduleViewer(context, this)
                notifyProcessTerminated(0)
            }
        }
        ProcessTerminatedListener.attach(process, context.environment.project)
        return process
    }

    private fun scheduleViewer(
        context: LatexRunStepContext,
        handler: ProcessHandler
    ) {
        val runConfig = context.runConfig
        val outputFilePath = context.session.resolvedOutputFilePath ?: return

        if (runConfig.isAutoCompiling) return

        val customCommand = stepConfig.customViewerCommand
        if (!customCommand.isNullOrBlank()) {
            val commandList = ParametersListUtil.parse(customCommand).toMutableList()
            val containsPlaceholder = commandList.any { it.contains("{pdf}") }
            if (containsPlaceholder) {
                for (i in commandList.indices) {
                    commandList[i] = commandList[i].replace("{pdf}", outputFilePath)
                }
            }
            else {
                commandList += outputFilePath
            }
            handler.addProcessListener(OpenCustomPdfViewerListener(commandList.toTypedArray(), runConfig = runConfig))
            return
        }

        val viewer = PdfViewer.availableViewers
            .firstOrNull { it.name == stepConfig.pdfViewerName }
            ?: PdfViewer.firstAvailableViewer
        val editor = context.environment.project.focusedTextEditor()?.editor
            ?: context.environment.project.selectedTextEditor()?.editor
        val line = editor?.document?.getLineNumber(editor.caretOffset())?.plus(1) ?: 0
        val currentFilePath = editor?.document?.let { FileDocumentManager.getInstance().getFile(it)?.path }
            ?: context.session.mainFile.path

        handler.addProcessListener(
            OpenViewerListener(
                viewer = viewer,
                outputPath = outputFilePath,
                session = context.session,
                sourceFilePath = currentFilePath,
                line = line,
                project = context.environment.project,
                focusAllowed = stepConfig.requireFocus,
            )
        )
    }
}
