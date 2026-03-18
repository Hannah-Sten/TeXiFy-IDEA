package nl.hannahsten.texifyidea.run.latex.step

import com.intellij.execution.ExecutionException
import com.intellij.execution.process.ProcessHandler
import com.intellij.openapi.actionSystem.ActionManager
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.util.execution.ParametersListUtil
import nl.hannahsten.texifyidea.TeXception
import nl.hannahsten.texifyidea.action.ForwardSearchAction
import nl.hannahsten.texifyidea.run.common.createCompilationHandler
import nl.hannahsten.texifyidea.run.latex.PdfViewerStepOptions
import nl.hannahsten.texifyidea.run.pdfviewer.CustomPdfViewer
import nl.hannahsten.texifyidea.run.pdfviewer.PdfViewer
import nl.hannahsten.texifyidea.util.caretOffset
import nl.hannahsten.texifyidea.util.focusedTextEditor
import nl.hannahsten.texifyidea.util.selectedTextEditor

internal class PdfViewerRunStep(
    private val stepConfig: PdfViewerStepOptions,
) : LatexRunStep {

    override val configId: String = stepConfig.id
    override val id: String = stepConfig.type
    override val displayName: String
        get() = stepConfig.displayName()

    override fun beforeStart(context: LatexRunStepContext) {
        if (shouldSkipInlineViewer(context)) {
            return
        }

        val resolved = resolveStandardViewerContext(context) ?: return
        try {
            openStandardViewer(resolved, context)
        }
        catch (_: TeXception) {
        }
    }

    @Throws(ExecutionException::class)
    override fun createProcess(context: LatexRunStepContext): ProcessHandler? {
        if (stepConfig.pdfViewerName != CustomPdfViewer.name) {
            return null
        }

        if (context.runConfig.isAutoCompiling) {
            return null
        }

        val outputFilePath = context.session.resolvedOutputFilePath ?: return null
        if (!isCustomViewerCommandConfigured()) {
            return null
        }

        val command = buildCustomViewerCommand(outputFilePath)
        if (command.isEmpty()) {
            throw ExecutionException("PDF viewer step has an empty custom viewer command.")
        }

        return createCompilationHandler(
            context = context,
            command = command,
            workingDirectory = context.session.workingDirectory,
        )
    }

    private fun shouldSkipInlineViewer(context: LatexRunStepContext): Boolean =
        context.runConfig.isAutoCompiling ||
            context.session.resolvedOutputFilePath == null ||
            isCustomViewerCommandConfigured() ||
            stepConfig.pdfViewerName == CustomPdfViewer.name

    private fun isCustomViewerCommandConfigured(): Boolean = !stepConfig.customViewerCommand.isNullOrBlank()

    private fun resolveStandardViewerContext(context: LatexRunStepContext): ResolvedViewerContext? {
        val outputFilePath = context.session.resolvedOutputFilePath ?: return null
        val viewer = PdfViewer.availableViewers
            .firstOrNull { it.name == stepConfig.pdfViewerName }
            ?: PdfViewer.firstAvailableViewer
        val editor = context.environment.project.focusedTextEditor()?.editor
            ?: context.environment.project.selectedTextEditor()?.editor
        val line = editor?.document?.getLineNumber(editor.caretOffset())?.plus(1) ?: 0
        val sourceFilePath = editor?.document?.let { FileDocumentManager.getInstance().getFile(it)?.path }
            ?: context.session.mainFile.path

        return ResolvedViewerContext(
            viewer = viewer,
            outputFilePath = outputFilePath,
            sourceFilePath = sourceFilePath,
            line = line,
        )
    }

    private fun openStandardViewer(
        resolved: ResolvedViewerContext,
        context: LatexRunStepContext,
    ) {
        resolved.viewer.openFile(
            resolved.outputFilePath,
            context.environment.project,
            focusAllowed = stepConfig.requireFocus,
        )
        resolved.viewer.forwardSearch(
            outputPath = resolved.outputFilePath,
            sourceFilePath = resolved.sourceFilePath,
            line = resolved.line,
            project = context.environment.project,
            focusAllowed = stepConfig.requireFocus,
        )
        (ActionManager.getInstance().getAction("texify.ForwardSearch") as? ForwardSearchAction)?.viewer = resolved.viewer
    }

    private fun buildCustomViewerCommand(outputFilePath: String): List<String> {
        val customCommand = stepConfig.customViewerCommand?.trim()?.takeIf(String::isNotBlank) ?: return emptyList()
        val commandList = ParametersListUtil.parse(customCommand).toMutableList()
        if (commandList.isEmpty()) {
            return emptyList()
        }

        val containsPlaceholder = commandList.any { it.contains("{pdf}") }
        if (containsPlaceholder) {
            for (i in commandList.indices) {
                commandList[i] = commandList[i].replace("{pdf}", outputFilePath)
            }
        }
        else {
            commandList += outputFilePath
        }

        return commandList
    }

    private data class ResolvedViewerContext(
        val viewer: PdfViewer,
        val outputFilePath: String,
        val sourceFilePath: String,
        val line: Int,
    )
}
