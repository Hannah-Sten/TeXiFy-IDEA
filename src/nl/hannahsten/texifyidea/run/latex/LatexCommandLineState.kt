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
import nl.hannahsten.texifyidea.util.*
import nl.hannahsten.texifyidea.util.files.psiFile
import nl.hannahsten.texifyidea.util.magic.PackageMagic
import java.io.File
import kotlin.io.path.Path
import kotlin.io.path.exists

/**
 * Run the run configuration: start the compile process and initiate forward search (when applicable).
 *
 * @author Sten Wessel
 */
open class LatexCommandLineState(environment: ExecutionEnvironment, private val runConfig: LatexRunConfiguration) : CommandLineState(environment) {

    private val programParamsConfigurator = ProgramParametersConfigurator()

    @Throws(ExecutionException::class)
    override fun startProcess(): ProcessHandler {
        val compiler = runConfig.compiler ?: throw ExecutionException("No valid compiler specified.")
        val mainFile = runConfig.mainFile ?: throw ExecutionException("Main file is not specified.")

        ProgressManager.getInstance().runProcessWithProgressSynchronously(
            {
                if (runConfig.outputPath.virtualFile == null || !runConfig.outputPath.virtualFile!!.exists()) {
                    runConfig.outputPath.getAndCreatePath()
                }
            },
            "Creating Output Directories...",
            false,
            runConfig.project
        )

        if (!runConfig.hasBeenRun) {
            // Show to the user what we're doing that takes so long (up to 30 seconds for a large project)
            // Unfortunately, this will block the UI (so you can't cancel it either), and I don't know how to run it in the background (e.g. Backgroundable) while still returning a ProcessHandler at the end of this method. Maybe it should be its own process.
            ProgressManager.getInstance().runProcessWithProgressSynchronously(
                { firstRunSetup(compiler) },
                "Generating Run Configuration...",
                false,
                runConfig.project
            )
        }

        val createdOutputDirectories = if (!runConfig.getLatexDistributionType().isMiktex(runConfig.project)) {
            runConfig.outputPath.updateOutputSubDirs()
        }
        else {
            setOf()
        }
        runConfig.filesToCleanUpIfEmpty.addAll(createdOutputDirectories)

        val handler = createHandler(mainFile, compiler)
        val isMakeindexNeeded = runMakeindexIfNeeded(handler, mainFile, runConfig.filesToCleanUp)
        runExternalToolsIfNeeded(handler)
        runConfig.hasBeenRun = true

        if (!isLastCompile(isMakeindexNeeded, handler)) return handler

        scheduleBibtexRunIfNeeded(handler)
        schedulePdfViewerIfNeeded(handler)
        if (runConfig.isAutoCompiling) {
            handler.addProcessListener(AutoCompileDoneListener())
            runConfig.isAutoCompiling = false
            // reset this flag, which will be set in each auto-compile
        }
        scheduleFileCleanup(runConfig.filesToCleanUp, runConfig.filesToCleanUpIfEmpty, handler)

        return handler
    }

    private fun createHandler(mainFile: VirtualFile, compiler: LatexCompiler): KillableProcessHandler {
        // Make sure to create the command after generating the bib run config (which might change the output path)
        val command: List<String> = compiler.getCommand(runConfig, environment.project)
            ?: throw ExecutionException("Compile command could not be created.")

        val workingDirectory = runConfig.getResolvedWorkingDirectory() ?: Path(mainFile.parent.path)
        if (workingDirectory.exists().not()) {
            Notification("LaTeX", "Could not find working directory", "The directory containing the main file could not be found: $workingDirectory", NotificationType.ERROR).notify(environment.project)
            throw ExecutionException("Could not find working directory $workingDirectory for file $mainFile")
        }

        val envVariables = runConfig.environmentVariables.envs.applyIf(runConfig.expandMacrosEnvVariables) {
            ExecutionManagerImpl.withEnvironmentDataContext(SimpleDataContext.getSimpleContext(CommonDataKeys.VIRTUAL_FILE, mainFile, environment.dataContext)).use {
                mapValues { programParamsConfigurator.expandPathAndMacros(it.value, null, runConfig.project) }
            }
        }

        // Windows has a maximum length of a command, possibly 32k characters (#3956), so we log this info in the exception
        if (SystemInfo.isWindows && command.sumOf { it.length } > 10_000) {
            throw ExecutionException("The following command was too long to run: ${command.joinToString(" ")}")
        }
        val commandLine = GeneralCommandLine(command).withWorkingDirectory(workingDirectory)
            .withParentEnvironmentType(GeneralCommandLine.ParentEnvironmentType.CONSOLE)
            .withEnvironment(envVariables)
        Log.debug("Executing ${commandLine.commandLineString} in $workingDirectory")
        val handler = runWithModalProgressBlocking(environment.project, "Creating command line process...") {
            KillableProcessHandler(commandLine)
        }

        // Reports exit code to run output window when command is terminated
        ProcessTerminatedListener.attach(handler, environment.project)

        return handler
    }

    /**
     * Do some long-running checks to generate the correct run configuration.
     */
    private fun firstRunSetup(compiler: LatexCompiler) {
        // Only at this moment we know the user really wants to run the run configuration, so only now we do the expensive check of
        // checking for bibliography commands.
        if (runConfig.bibRunConfigs.isEmpty() &&
            !compiler.includesBibtex &&
            // citation-style-language package does not need a bibtex run configuration
            runConfig.mainFile?.psiFile(runConfig.project)?.includedPackagesInFileset()?.contains(LatexLib.CITATION_STYLE_LANGUAGE)?.not() == true
        ) {
            // Generating a bib run config involves PSI access, which requires a read action.
            runReadAction {
                // If the index is not ready, we cannot check if a bib run config is needed, so skip this and run the main run config anyway
                if (!DumbService.getInstance(runConfig.project).isDumb) {
                    Log.debug("Not generating bibtex run config because index is not ready")
                    runConfig.generateBibRunConfig()
                }
            }

            runConfig.bibRunConfigs.forEach {
                // Pass necessary latex run configurations settings to the bibtex run configuration.
                (it.configuration as? BibtexRunConfiguration)?.apply {
                    // Check if the aux, out, or src folder should be used as bib working dir.
                    // This involves a synchronous refreshAndFindFileByPath, and hence cannot be done in a read action
                    this.bibWorkingDir = runConfig.getAuxilDirectory()
                }
            }
        }
    }

    private fun runExternalToolsIfNeeded(
        handler: KillableProcessHandler
    ): Boolean {
        val isAnyExternalToolNeeded = if (!runConfig.hasBeenRun) {
            // This is a relatively expensive check
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

    private fun runMakeindexIfNeeded(handler: KillableProcessHandler, mainFile: VirtualFile, filesToCleanUp: MutableList<File>): Boolean {
        var isMakeindexNeeded = false

        // To find out whether makeindex is needed is relatively expensive,
        // so we only do this the first time
        if (!runConfig.hasBeenRun) {
            val commandsInFileSet = NewCommandsIndex.getAllKeys(LatexProjectStructure.getFilesetScopeFor(mainFile, environment.project))
            // Option 1 in http://mirrors.ctan.org/macros/latex/contrib/glossaries/glossariesbegin.pdf
            val usesTexForGlossaries = "\\makenoidxglossaries" in commandsInFileSet

            if (usesTexForGlossaries) {
                runConfig.compileTwice = true
            }

            // If no index package is used, we assume we won't have to run makeindex
            val includedPackages = runConfig.mainFile
                ?.psiFile(runConfig.project)
                ?.includedPackagesInFileset()
                ?: setOf()

            isMakeindexNeeded = includedPackages.intersect(PackageMagic.index + PackageMagic.glossary).isNotEmpty() && runConfig.compiler?.includesMakeindex == false && !usesTexForGlossaries

            // Some packages do handle makeindex themselves
            // Note that when you use imakeidx with the noautomatic option it won't, but we don't check for that
            if (includedPackages.contains(LatexLib.IMAKEIDX) && !runConfig.usesAuxilOrOutDirectory()) {
                isMakeindexNeeded = false
            }
        }

        // Run makeindex when applicable
        if (runConfig.isFirstRunConfig && (runConfig.makeindexRunConfigs.isNotEmpty() || isMakeindexNeeded)) {
            handler.addProcessListener(RunMakeindexListener(runConfig, environment, filesToCleanUp))
        }

        return isMakeindexNeeded
    }

    private fun isLastCompile(isMakeindexNeeded: Boolean, handler: KillableProcessHandler): Boolean {
        // If there is no bibtex/makeindex involved and we don't need to compile twice, then this is the last compile
        if (runConfig.bibRunConfigs.isEmpty() && !isMakeindexNeeded && runConfig.externalToolRunConfigs.isEmpty()) {
            if (!runConfig.compileTwice) {
                runConfig.isLastRunConfig = true
            }

            // Schedule the second compile only if this is the first compile
            @Suppress("KotlinConstantConditions")
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

            // Only run latex after the last one
            if (index == runConfig.bibRunConfigs.size - 1) {
                handler.addProcessListener(RunBibtexListener(bibSettings, runConfig, environment, true))
            }
            else {
                handler.addProcessListener(RunBibtexListener(bibSettings, runConfig, environment, false))
            }
        }
    }

    private fun schedulePdfViewerIfNeeded(handler: KillableProcessHandler) {
        // Do not schedule to open the pdf viewer when this is not the last run config in the chain or it is an auto compile
        if (runConfig.isLastRunConfig && !runConfig.isAutoCompiling) {
            addOpenViewerListener(handler, runConfig.requireFocus)
        }
    }

    private fun scheduleFileCleanup(filesToCleanUp: MutableList<File>, filesToCleanUpIfEmpty: Set<File>, handler: KillableProcessHandler) {
        if (runConfig.isLastRunConfig) {
            handler.addProcessListener(FileCleanupListener(filesToCleanUp, filesToCleanUpIfEmpty))
        }
    }

    /**
     * Add a certain process listener for opening the right pdf viewer depending on settings and OS.
     *
     * @param focusAllowed Whether focussing the pdf viewer is allowed. If not, it may happen forward search is not executed (in case the pdf viewer does not support forward search without changing focus).
     */
    private fun addOpenViewerListener(handler: ProcessHandler, focusAllowed: Boolean = true) {
        // First check if the user specified a custom viewer, if not then try other supported viewers
        if (!runConfig.viewerCommand.isNullOrEmpty()) {
            // Split user command on spaces, then replace {pdf} if needed
            val commandString = runConfig.viewerCommand!!

            // Split on spaces
            val commandList = commandString.split(" ").toMutableList()

            val containsPlaceholder = commandList.contains("{pdf}")

            if (containsPlaceholder) {
                // Replace placeholder
                for (i in 0 until commandList.size) {
                    if (commandList[i].contains("{pdf}")) {
                        commandList[i] = commandList[i].replace("{pdf}", runConfig.outputFilePath)
                    }
                }
            }
            else {
                // If no placeholder was used, assume the path is the final argument
                commandList += runConfig.outputFilePath
            }
            handler.addProcessListener(OpenCustomPdfViewerListener(commandList.toTypedArray(), runConfig = runConfig))
            return
        }
        val pdfViewer = runConfig.pdfViewer
        // Do nothing if the user selected that they do not want a viewer to open.

        if (pdfViewer != null) {
            // the pdf viewer is well defined, so we can use it
            scheduleForwardSearchAfterCompile(pdfViewer, handler, runConfig, environment, focusAllowed)
        }
    }

    /**
     * Execute forward search when the process is done.
     *
     * In the case that no tex file is open, forward search from the first line of the main file that is selected in the
     * run config.
     */
    fun scheduleForwardSearchAfterCompile(viewer: PdfViewer, handler: ProcessHandler, runConfig: LatexRunConfiguration, environment: ExecutionEnvironment, focusAllowed: Boolean = true) {
        // We have to find the file and line number before scheduling the forward search.
        val editor = environment.project.focusedTextEditor()?.editor ?: environment.project.selectedTextEditor()?.editor

        // Get the line number in the currently open file
        val line = editor?.document?.getLineNumber(editor.caretOffset())?.plus(1) ?: 0

        // Get the currently open file to use for forward search.
        val currentPsiFile = editor?.document?.psiFile(environment.project)
            // Get the main file from the run configuration as a fallback.
            ?: runConfig.mainFile?.psiFile(environment.project)
            ?: return

        // Set the OpenViewerListener to execute when the compilation is done.
        handler.addProcessListener(OpenViewerListener(viewer, runConfig, currentPsiFile.virtualFile.path, line, environment.project, focusAllowed))
    }
}
