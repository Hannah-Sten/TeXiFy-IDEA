package nl.hannahsten.texifyidea.run.latex

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
import com.intellij.openapi.application.runReadAction
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.project.DumbService
import com.intellij.openapi.util.SystemInfo
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.platform.ide.progress.runWithModalProgressBlocking
import com.intellij.util.applyIf
import nl.hannahsten.texifyidea.editor.autocompile.AutoCompileDoneListener
import nl.hannahsten.texifyidea.index.NewCommandsIndex
import nl.hannahsten.texifyidea.index.projectstructure.LatexProjectStructure
import nl.hannahsten.texifyidea.lang.LatexLib
import nl.hannahsten.texifyidea.run.FileCleanupListener
import nl.hannahsten.texifyidea.run.OpenCustomPdfViewerListener
import nl.hannahsten.texifyidea.run.bibtex.BibtexRunConfiguration
import nl.hannahsten.texifyidea.run.bibtex.RunBibtexListener
import nl.hannahsten.texifyidea.run.compiler.LatexCompiler
import nl.hannahsten.texifyidea.run.latex.externaltool.RunExternalToolListener
import nl.hannahsten.texifyidea.run.makeindex.RunMakeindexListener
import nl.hannahsten.texifyidea.run.pdfviewer.OpenViewerListener
import nl.hannahsten.texifyidea.run.pdfviewer.PdfViewer
import nl.hannahsten.texifyidea.util.Log
import nl.hannahsten.texifyidea.util.focusedTextEditor
import nl.hannahsten.texifyidea.util.files.psiFile
import nl.hannahsten.texifyidea.util.includedPackagesInFileset
import nl.hannahsten.texifyidea.util.magic.PackageMagic
import nl.hannahsten.texifyidea.util.selectedTextEditor
import kotlin.io.path.Path
import kotlin.io.path.exists

/**
 * Run the run configuration: start the compile process and initiate forward search (when applicable).
 */
open class LatexCommandLineState(environment: ExecutionEnvironment, private val runConfig: LatexRunConfiguration) : CommandLineState(environment) {

    companion object {
        private const val FLAG_MAKEINDEX_NEEDED = "makeindexNeeded"
    }

    private val programParamsConfigurator = ProgramParametersConfigurator()
    private val pipeline: LatexCompilationPipeline = LatexPipeline()

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
        val workingDirectoryPath = runConfig.getResolvedWorkingDirectory() ?: mainFile.parent.path
        val workingDirectory = Path(workingDirectoryPath)
        if (workingDirectory.exists().not()) {
            Notification("LaTeX", "Could not find working directory", "The directory containing the main file could not be found: $workingDirectoryPath", NotificationType.ERROR).notify(environment.project)
            throw ExecutionException("Could not find working directory $workingDirectoryPath for file $mainFile")
        }

        val envVariables = runConfig.environmentVariables.envs.applyIf(runConfig.expandMacrosEnvVariables) {
            ExecutionManagerImpl.withEnvironmentDataContext(SimpleDataContext.getSimpleContext(CommonDataKeys.VIRTUAL_FILE, mainFile, environment.dataContext)).use {
                mapValues { programParamsConfigurator.expandPathAndMacros(it.value, null, runConfig.project) }
            }
        }

        if (SystemInfo.isWindows && command.sumOf { it.length } > 10_000) {
            throw ExecutionException("The following command was too long to run: ${command.joinToString(" ")}")
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

    private fun firstRunSetup(compiler: LatexCompiler) {
        if (runConfig.bibRunConfigs.isEmpty() &&
            !compiler.includesBibtex &&
            runConfig.mainFile?.psiFile(runConfig.project)?.includedPackagesInFileset()?.contains(LatexLib.CITATION_STYLE_LANGUAGE)?.not() == true
        ) {
            runReadAction {
                if (!DumbService.getInstance(runConfig.project).isDumb) {
                    Log.debug("Not generating bibtex run config because index is not ready")
                    runConfig.generateBibRunConfig()
                }
            }

            runConfig.bibRunConfigs.forEach {
                (it.configuration as? BibtexRunConfiguration)?.apply {
                    this.bibWorkingDir = runConfig.getAuxilDirectory()
                }
            }
        }
    }

    private fun runExternalToolsIfNeeded(handler: KillableProcessHandler): Boolean {
        val isAnyExternalToolNeeded = if (!runConfig.hasBeenRun) {
            RunExternalToolListener.getRequiredExternalTools(runConfig.mainFile, runConfig.project).isNotEmpty()
        }
        else {
            false
        }

        if (runConfig.isFirstRunConfig && (runConfig.externalToolRunConfigs.isNotEmpty() || isAnyExternalToolNeeded)) {
            handler.addProcessListener(RunExternalToolListener(runConfig, environment))
        }

        return isAnyExternalToolNeeded
    }

    private fun runMakeindexIfNeeded(handler: KillableProcessHandler, mainFile: VirtualFile, filesToCleanUp: MutableList<java.io.File>): Boolean {
        var isMakeindexNeeded = false

        if (!runConfig.hasBeenRun) {
            val commandsInFileSet = NewCommandsIndex.getAllKeys(LatexProjectStructure.getFilesetScopeFor(mainFile, environment.project))
            val usesTexForGlossaries = "\\makenoidxglossaries" in commandsInFileSet

            if (usesTexForGlossaries) {
                runConfig.compileTwice = true
            }

            val includedPackages = runConfig.mainFile
                ?.psiFile(runConfig.project)
                ?.includedPackagesInFileset()
                ?: setOf()

            isMakeindexNeeded = includedPackages.intersect(PackageMagic.index + PackageMagic.glossary).isNotEmpty() && runConfig.compiler?.includesMakeindex == false && !usesTexForGlossaries

            if (includedPackages.contains(LatexLib.IMAKEIDX) && !runConfig.usesAuxilOrOutDirectory()) {
                isMakeindexNeeded = false
            }
        }

        if (runConfig.isFirstRunConfig && (runConfig.makeindexRunConfigs.isNotEmpty() || isMakeindexNeeded)) {
            handler.addProcessListener(RunMakeindexListener(runConfig, environment, filesToCleanUp))
        }

        return isMakeindexNeeded
    }

    private fun isLastCompile(isMakeindexNeeded: Boolean, handler: KillableProcessHandler): Boolean {
        if (runConfig.bibRunConfigs.isEmpty() && !isMakeindexNeeded && runConfig.externalToolRunConfigs.isEmpty()) {
            if (!runConfig.compileTwice) {
                runConfig.isLastRunConfig = true
            }

            if (!runConfig.isLastRunConfig && runConfig.compileTwice) {
                handler.addProcessListener(RunLatexListener(runConfig, environment))
                return false
            }
        }

        return true
    }

    private fun scheduleBibtexRunIfNeeded(handler: KillableProcessHandler) {
        runConfig.bibRunConfigs.forEachIndexed { index, bibSettings ->
            if (!runConfig.isFirstRunConfig) {
                return@forEachIndexed
            }

            if (index == runConfig.bibRunConfigs.size - 1) {
                handler.addProcessListener(RunBibtexListener(bibSettings, runConfig, environment, true))
            }
            else {
                handler.addProcessListener(RunBibtexListener(bibSettings, runConfig, environment, false))
            }
        }
    }

    private fun schedulePdfViewerIfNeeded(handler: KillableProcessHandler) {
        if (runConfig.isLastRunConfig && !runConfig.isAutoCompiling) {
            addOpenViewerListener(handler, runConfig.requireFocus)
        }
    }

    private fun scheduleFileCleanup(context: LatexExecutionContext, handler: KillableProcessHandler) {
        if (runConfig.isLastRunConfig && (context.transientFilesToClean.isNotEmpty() || context.emptyDirsToCleanup.isNotEmpty())) {
            handler.addProcessListener(FileCleanupListener(context.transientFilesToClean, context.emptyDirsToCleanup))
        }
    }

    private fun addOpenViewerListener(handler: ProcessHandler, focusAllowed: Boolean = true) {
        if (!runConfig.viewerCommand.isNullOrEmpty()) {
            val commandList = runConfig.viewerCommand!!.split(" ").toMutableList()
            val containsPlaceholder = commandList.contains("{pdf}")

            if (containsPlaceholder) {
                for (i in commandList.indices) {
                    if (commandList[i].contains("{pdf}")) {
                        commandList[i] = commandList[i].replace("{pdf}", runConfig.outputFilePath)
                    }
                }
            }
            else {
                commandList += runConfig.outputFilePath
            }
            handler.addProcessListener(OpenCustomPdfViewerListener(commandList.toTypedArray(), runConfig = runConfig))
            return
        }

        val pdfViewer = runConfig.pdfViewer
        if (pdfViewer != null) {
            scheduleForwardSearchAfterCompile(pdfViewer, handler, runConfig, environment, focusAllowed)
        }
    }

    fun scheduleForwardSearchAfterCompile(
        viewer: PdfViewer,
        handler: ProcessHandler,
        runConfig: LatexCompilationRunConfiguration,
        environment: ExecutionEnvironment,
        focusAllowed: Boolean = true,
    ) {
        val editor = environment.project.focusedTextEditor()?.editor ?: environment.project.selectedTextEditor()?.editor
        val line = editor?.document?.getLineNumber(editor.caretModel.offset)?.plus(1) ?: 0

        val currentPsiFile = editor?.document?.psiFile(environment.project)
            ?: runConfig.mainFile?.psiFile(environment.project)
            ?: return

        handler.addProcessListener(OpenViewerListener(viewer, runConfig, currentPsiFile.virtualFile.path, line, environment.project, focusAllowed))
    }

    private inner class LatexPipeline : LatexCompilationPipeline {

        override fun prepare(runConfig: LatexCompilationRunConfiguration, environment: ExecutionEnvironment, context: LatexExecutionContext) {
            val compiler = this@LatexCommandLineState.runConfig.compiler ?: throw ExecutionException("No valid compiler specified.")

            ProgressManager.getInstance().runProcessWithProgressSynchronously(
                {
                    if (this@LatexCommandLineState.runConfig.outputPath.virtualFile == null || !this@LatexCommandLineState.runConfig.outputPath.virtualFile!!.exists()) {
                        this@LatexCommandLineState.runConfig.outputPath.getAndCreatePath()
                    }
                },
                "Creating Output Directories...",
                false,
                this@LatexCommandLineState.runConfig.project,
            )

            if (!this@LatexCommandLineState.runConfig.hasBeenRun) {
                ProgressManager.getInstance().runProcessWithProgressSynchronously(
                    { firstRunSetup(compiler) },
                    "Generating Run Configuration...",
                    false,
                    this@LatexCommandLineState.runConfig.project,
                )
            }

            val createdOutputDirectories = if (!this@LatexCommandLineState.runConfig.getLatexDistributionType().isMiktex(this@LatexCommandLineState.runConfig.project)) {
                this@LatexCommandLineState.runConfig.outputPath.updateOutputSubDirs()
            }
            else {
                setOf()
            }
            context.emptyDirsToCleanup.addAll(createdOutputDirectories)
        }

        override fun buildCommand(runConfig: LatexCompilationRunConfiguration, environment: ExecutionEnvironment, context: LatexExecutionContext): List<String> = LatexCommandBuilder.build(this@LatexCommandLineState.runConfig, environment.project)
            ?: throw ExecutionException("Compile command could not be created.")

        override fun finalize(runConfig: LatexCompilationRunConfiguration, handler: KillableProcessHandler, environment: ExecutionEnvironment, context: LatexExecutionContext) {
            val mainFile = this@LatexCommandLineState.runConfig.mainFile ?: return

            context.setFlag(FLAG_MAKEINDEX_NEEDED, runMakeindexIfNeeded(handler, mainFile, context.transientFilesToClean))
            runExternalToolsIfNeeded(handler)

            if (!isLastCompile(context.getFlag(FLAG_MAKEINDEX_NEEDED), handler)) {
                return
            }

            scheduleBibtexRunIfNeeded(handler)
            schedulePdfViewerIfNeeded(handler)
            scheduleFileCleanup(context, handler)
        }
    }
}
