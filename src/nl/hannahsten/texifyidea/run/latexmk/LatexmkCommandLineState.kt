package nl.hannahsten.texifyidea.run.latexmk

import com.intellij.execution.ExecutionException
import com.intellij.execution.configurations.CommandLineState
import com.intellij.execution.configurations.GeneralCommandLine
import com.intellij.execution.impl.ExecutionManagerImpl
import com.intellij.execution.process.KillableProcessHandler
import com.intellij.execution.process.ProcessHandler
import com.intellij.execution.process.ProcessTerminatedListener
import com.intellij.execution.runners.ExecutionEnvironment
import com.intellij.execution.util.ProgramParametersConfigurator
import com.intellij.notification.Notification
import com.intellij.notification.NotificationType
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.actionSystem.impl.SimpleDataContext
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.platform.ide.progress.runWithModalProgressBlocking
import com.intellij.util.applyIf
import nl.hannahsten.texifyidea.editor.autocompile.AutoCompileDoneListener
import nl.hannahsten.texifyidea.run.OpenCustomPdfViewerListener
import nl.hannahsten.texifyidea.run.latex.LatexCompilationPipeline
import nl.hannahsten.texifyidea.run.latex.LatexCompilationRunConfiguration
import nl.hannahsten.texifyidea.run.latex.LatexExecutionContext
import nl.hannahsten.texifyidea.run.pdfviewer.OpenViewerListener
import nl.hannahsten.texifyidea.util.Log
import nl.hannahsten.texifyidea.util.files.psiFile
import java.nio.file.Path
import kotlin.io.path.exists

class LatexmkCommandLineState(
    private val environment: ExecutionEnvironment,
    private val runConfig: LatexmkRunConfiguration,
) : CommandLineState(environment) {

    private val pipeline: LatexCompilationPipeline = LatexmkPipeline()
    private val programParamsConfigurator = ProgramParametersConfigurator()

    @Throws(ExecutionException::class)
    override fun startProcess(): ProcessHandler {
        val mainFile = runConfig.mainFile ?: throw ExecutionException("Main file is not specified.")
        val context = LatexExecutionContext()

        pipeline.prepare(runConfig, environment, context)
        val command = pipeline.buildCommand(runConfig, environment, context)

        val handler = createHandler(mainFile, command)
        runConfig.hasBeenRun = true

        pipeline.finalize(runConfig, handler, environment, context)

        if (runConfig.isAutoCompiling) {
            handler.addProcessListener(AutoCompileDoneListener())
            runConfig.isAutoCompiling = false
        }

        return handler
    }

    private fun createHandler(mainFile: VirtualFile, command: List<String>): KillableProcessHandler {
        val workingDirectory = runConfig.getResolvedWorkingDirectory() ?: Path.of(mainFile.parent.path)
        if (workingDirectory.exists().not()) {
            Notification(
                "LaTeX",
                "Could not find working directory",
                "The directory containing the main file could not be found: $workingDirectory",
                NotificationType.ERROR,
            ).notify(environment.project)
            throw ExecutionException("Could not find working directory $workingDirectory for file $mainFile")
        }

        val envVariables = runConfig.environmentVariables.envs.applyIf(runConfig.expandMacrosEnvVariables) {
            ExecutionManagerImpl.withEnvironmentDataContext(SimpleDataContext.getSimpleContext(CommonDataKeys.VIRTUAL_FILE, mainFile, environment.dataContext)).use {
                mapValues { programParamsConfigurator.expandPathAndMacros(it.value, null, runConfig.project) }
            }
        }

        val commandLine = GeneralCommandLine(command)
            .withWorkingDirectory(workingDirectory)
            .withParentEnvironmentType(GeneralCommandLine.ParentEnvironmentType.CONSOLE)
            .withEnvironment(envVariables)

        Log.debug("Executing ${commandLine.commandLineString} in $workingDirectory")

        val handler = runWithModalProgressBlocking(environment.project, "Creating command line process...") {
            KillableProcessHandler(commandLine)
        }
        ProcessTerminatedListener.attach(handler, environment.project)
        return handler
    }
}

private class LatexmkPipeline : LatexCompilationPipeline {

    override fun prepare(runConfig: LatexCompilationRunConfiguration, environment: ExecutionEnvironment, context: LatexExecutionContext) {
        ProgressManager.getInstance().runProcessWithProgressSynchronously(
            {
                if (runConfig is LatexmkRunConfiguration) {
                    LatexmkPathResolver.ensureDirectories(runConfig)
                }
            },
            "Creating Output Directories...",
            false,
            runConfig.project,
        )
    }

    override fun buildCommand(runConfig: LatexCompilationRunConfiguration, environment: ExecutionEnvironment, context: LatexExecutionContext): List<String> {
        val latexmkRunConfig = runConfig as? LatexmkRunConfiguration
            ?: throw ExecutionException("Latexmk pipeline expects LatexmkRunConfiguration.")
        return LatexmkCommandBuilder.buildCommand(latexmkRunConfig, environment.project)
            ?: throw ExecutionException("Compile command could not be created.")
    }

    override fun finalize(runConfig: LatexCompilationRunConfiguration, handler: KillableProcessHandler, environment: ExecutionEnvironment, context: LatexExecutionContext) {
        if (runConfig.isAutoCompiling) return

        if (!runConfig.viewerCommand.isNullOrEmpty()) {
            val commandList = runConfig.viewerCommand!!.split(" ").toMutableList()
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
        val currentPsiFile = runConfig.mainFile?.psiFile(environment.project) ?: return
        handler.addProcessListener(OpenViewerListener(pdfViewer, runConfig, currentPsiFile.virtualFile.path, 1, environment.project, runConfig.requireFocus))
    }
}
