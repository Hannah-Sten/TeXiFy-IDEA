package nl.hannahsten.texifyidea.run.latexmk

import com.intellij.execution.ExecutionException
import com.intellij.execution.configurations.CommandLineState
import com.intellij.execution.process.KillableProcessHandler
import com.intellij.execution.process.ProcessHandler
import com.intellij.execution.runners.ExecutionEnvironment
import com.intellij.execution.util.ProgramParametersConfigurator
import com.intellij.util.execution.ParametersListUtil
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.vfs.VirtualFile
import nl.hannahsten.texifyidea.editor.autocompile.AutoCompileDoneListener
import nl.hannahsten.texifyidea.run.common.createCompilationHandler
import nl.hannahsten.texifyidea.run.OpenCustomPdfViewerListener
import nl.hannahsten.texifyidea.run.pdfviewer.OpenViewerListener
import nl.hannahsten.texifyidea.run.pdfviewer.PdfViewer
import nl.hannahsten.texifyidea.util.caretOffset
import nl.hannahsten.texifyidea.util.focusedTextEditor
import nl.hannahsten.texifyidea.util.selectedTextEditor

class LatexmkCommandLineState(
    private val environment: ExecutionEnvironment,
    private val runConfig: LatexmkRunConfiguration,
) : CommandLineState(environment) {

    private val programParamsConfigurator = ProgramParametersConfigurator()

    @Throws(ExecutionException::class)
    override fun startProcess(): ProcessHandler {
        val mainFile = runConfig.resolveMainFileIfNeeded() ?: throw ExecutionException("Main file is not specified.")

        prepare()
        val command = buildCommand()

        val handler = createHandler(mainFile, command)
        runConfig.hasBeenRun = true

        finalize(handler)

        if (runConfig.isAutoCompiling) {
            handler.addProcessListener(AutoCompileDoneListener())
            runConfig.isAutoCompiling = false
        }

        return handler
    }

    private fun prepare() {
        ProgressManager.getInstance().runProcessWithProgressSynchronously(
            {
                LatexmkPathResolver.ensureDirectories(runConfig)
            },
            "Creating Output Directories...",
            false,
            runConfig.project,
        )
    }

    @Throws(ExecutionException::class)
    private fun buildCommand(): List<String> =
        LatexmkCommandBuilder.buildCommand(runConfig, environment.project)
            ?: throw ExecutionException("Compile command could not be created.")

    private fun finalize(handler: KillableProcessHandler) {
        if (runConfig.isAutoCompiling) return

        if (!runConfig.viewerCommand.isNullOrEmpty()) {
            val commandList = ParametersListUtil.parse(runConfig.viewerCommand!!).toMutableList()
            val containsPlaceholder = commandList.contains("{pdf}")
            if (containsPlaceholder) {
                for (i in commandList.indices) {
                    if (commandList[i].contains("{pdf}")) {
                        commandList[i] = commandList[i].replace("{pdf}", runConfig.getOutputFilePath())
                    }
                }
            }
            else {
                commandList += runConfig.getOutputFilePath()
            }
            handler.addProcessListener(OpenCustomPdfViewerListener(commandList.toTypedArray(), runConfig = runConfig))
            return
        }

        val pdfViewer = runConfig.pdfViewer ?: return
        scheduleForwardSearchAfterCompile(pdfViewer, handler)
    }

    private fun scheduleForwardSearchAfterCompile(viewer: PdfViewer, handler: ProcessHandler) {
        val editor = findForwardSearchEditor()
        val line = resolveForwardSearchLine(editor)
        val sourcePath = resolveForwardSearchSourcePath(editor) ?: return

        handler.addProcessListener(OpenViewerListener(viewer, runConfig, sourcePath, line, environment.project, runConfig.requireFocus))
    }

    private fun findForwardSearchEditor() =
        environment.project.focusedTextEditor()?.editor ?: environment.project.selectedTextEditor()?.editor

    private fun resolveForwardSearchLine(editor: Editor?): Int =
        editor?.document?.getLineNumber(editor.caretOffset())?.plus(1) ?: 1

    private fun resolveForwardSearchSourcePath(editor: Editor?): String? {
        val fallbackPath = runConfig.resolveMainFileIfNeeded()?.path
        val editorPath = editor?.document?.let { FileDocumentManager.getInstance().getFile(it)?.path }
        return editorPath ?: fallbackPath
    }

    private fun createHandler(mainFile: VirtualFile, command: List<String>): KillableProcessHandler = createCompilationHandler(
        environment = environment,
        mainFile = mainFile,
        command = command,
        workingDirectory = runConfig.getResolvedWorkingDirectory(),
        expandMacrosEnvVariables = runConfig.expandMacrosEnvVariables,
        envs = runConfig.environmentVariables.envs,
        expandEnvValue = { value -> programParamsConfigurator.expandPathAndMacros(value, null, runConfig.project) ?: value },
    )
}
