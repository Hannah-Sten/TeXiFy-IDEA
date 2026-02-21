package nl.hannahsten.texifyidea.run.latex

import com.intellij.execution.ExecutionException
import com.intellij.execution.configurations.CommandLineState
import com.intellij.execution.process.KillableProcessHandler
import com.intellij.execution.process.ProcessHandler
import com.intellij.execution.runners.ExecutionEnvironment
import com.intellij.execution.util.ProgramParametersConfigurator
import com.intellij.util.execution.ParametersListUtil
import com.intellij.openapi.application.ReadAction
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.project.DumbService
import com.intellij.openapi.vfs.VirtualFile
import nl.hannahsten.texifyidea.editor.autocompile.AutoCompileDoneListener
import nl.hannahsten.texifyidea.index.NewCommandsIndex
import nl.hannahsten.texifyidea.index.projectstructure.LatexProjectStructure
import nl.hannahsten.texifyidea.lang.LatexLib
import nl.hannahsten.texifyidea.run.FileCleanupListener
import nl.hannahsten.texifyidea.run.OpenCustomPdfViewerListener
import nl.hannahsten.texifyidea.run.bibtex.BibtexRunConfiguration
import nl.hannahsten.texifyidea.run.bibtex.RunBibtexListener
import nl.hannahsten.texifyidea.run.common.createCompilationHandler
import nl.hannahsten.texifyidea.run.compiler.LatexCompiler
import nl.hannahsten.texifyidea.run.latex.externaltool.RunExternalToolListener
import nl.hannahsten.texifyidea.run.makeindex.RunMakeindexListener
import nl.hannahsten.texifyidea.run.pdfviewer.OpenViewerListener
import nl.hannahsten.texifyidea.run.pdfviewer.PdfViewer
import nl.hannahsten.texifyidea.util.*
import nl.hannahsten.texifyidea.util.files.psiFile
import nl.hannahsten.texifyidea.util.magic.PackageMagic

/**
 * Run the run configuration: start the compile process and initiate forward search (when applicable).
 *
 * @author Sten Wessel
 */
open class LatexCommandLineState(environment: ExecutionEnvironment, private val runConfig: LatexRunConfiguration) : CommandLineState(environment) {

    private val programParamsConfigurator = ProgramParametersConfigurator()
    private val executionState = runConfig.executionState

    @Throws(ExecutionException::class)
    override fun startProcess(): ProcessHandler {
        val compiler = runConfig.compiler ?: throw ExecutionException("No valid compiler specified.")
        LatexExecutionStateInitializer.initialize(runConfig, environment, executionState)
        val mainFile = executionState.resolvedMainFile ?: throw ExecutionException("Main file cannot be resolved")

        return if (compiler == LatexCompiler.LATEXMK) {
            startLatexmkProcess(mainFile, compiler)
        }
        else {
            startClassicProcess(mainFile, compiler)
        }
    }

    private fun startLatexmkProcess(mainFile: VirtualFile, compiler: LatexCompiler): ProcessHandler {
        val handler = createHandler(mainFile, compiler)
        executionState.markHasRun()

        if (!isLastCompile(isMakeindexNeeded = false, handler)) return handler

        schedulePdfViewerIfNeeded(handler)
        if (runConfig.isAutoCompiling) {
            handler.addProcessListener(AutoCompileDoneListener())
            runConfig.isAutoCompiling = false
            // reset this flag, which will be set in each auto-compile
        }

        return handler
    }

    private fun startClassicProcess(mainFile: VirtualFile, compiler: LatexCompiler): ProcessHandler {
        if (!executionState.hasBeenRun) {
            firstRunSetup(compiler)
        }

        val handler = createHandler(mainFile, compiler)
        val isMakeindexNeeded = runMakeindexIfNeeded(handler, mainFile)
        runExternalToolsIfNeeded(handler)
        executionState.markHasRun()

        if (!isLastCompile(isMakeindexNeeded, handler)) return handler

        scheduleBibtexRunIfNeeded(handler)
        schedulePdfViewerIfNeeded(handler)
        if (runConfig.isAutoCompiling) {
            handler.addProcessListener(AutoCompileDoneListener())
            runConfig.isAutoCompiling = false
        }
        scheduleFileCleanup(handler)
        return handler
    }

    private fun createHandler(mainFile: VirtualFile, compiler: LatexCompiler): KillableProcessHandler {
        // Make sure to create the command after generating the bib run config (which might change the output path)
        val command: List<String> = compiler.getCommand(runConfig, environment.project)
            ?: throw ExecutionException("Compile command could not be created.")

        return createCompilationHandler(
            environment = environment,
            mainFile = mainFile,
            command = command,
            workingDirectory = executionState.resolvedWorkingDirectory,
            expandMacrosEnvVariables = runConfig.expandMacrosEnvVariables,
            envs = runConfig.environmentVariables.envs,
            expandEnvValue = { value -> programParamsConfigurator.expandPathAndMacros(value, null, runConfig.project) ?: value },
        )
    }

    /**
     * Do some long-running checks to generate the correct run configuration.
     */
    private fun firstRunSetup(compiler: LatexCompiler) {
        if (compiler == LatexCompiler.LATEXMK) {
            return
        }

        val usesCsl = ReadAction.compute<Boolean, RuntimeException> {
            executionState.resolvedMainFile?.psiFile(runConfig.project)?.includedPackagesInFileset()?.contains(LatexLib.CITATION_STYLE_LANGUAGE) == true
        }

        // Only at this moment we know the user really wants to run the run configuration, so only now we do the expensive check of
        // checking for bibliography commands.
        if (runConfig.bibRunConfigs.isEmpty() &&
            !compiler.includesBibtex &&
            // citation-style-language package does not need a bibtex run configuration
            !usesCsl
        ) {
            // Generating a bib run config involves PSI access, which requires a read action.
            ReadAction.run<RuntimeException> {
                // If the index is not ready, we cannot check if a bib run config is needed, so skip this and run the main run config anyway
                if (!DumbService.getInstance(runConfig.project).isDumb) {
                    Log.debug("Generating bibtex run config during first run setup")
                    runConfig.generateBibRunConfig()
                }
            }

            runConfig.bibRunConfigs.forEach {
                // Pass necessary latex run configurations settings to the bibtex run configuration.
                (it.configuration as? BibtexRunConfiguration)?.apply {
                    // Check if the aux, out, or src folder should be used as bib working dir.
                    // This involves a synchronous refreshAndFindFileByPath, and hence cannot be done in a read action
                    this.bibWorkingDir = executionState.resolvedAuxDir ?: executionState.resolvedOutputDir
                }
            }
        }
    }

    private fun runExternalToolsIfNeeded(
        handler: KillableProcessHandler
    ): Boolean {
        val isAnyExternalToolNeeded = if (!executionState.hasBeenRun) {
            // This is a relatively expensive check
            RunExternalToolListener.getRequiredExternalTools(executionState.resolvedMainFile, runConfig.project).isNotEmpty()
        }
        else {
            false
        }

        if (executionState.isFirstRunConfig && (runConfig.externalToolRunConfigs.isNotEmpty() || isAnyExternalToolNeeded)) {
            handler.addProcessListener(RunExternalToolListener(runConfig, environment, executionState))
        }

        return isAnyExternalToolNeeded
    }

    private fun runMakeindexIfNeeded(handler: KillableProcessHandler, mainFile: VirtualFile): Boolean {
        var isMakeindexNeeded = false

        // To find out whether makeindex is needed is relatively expensive,
        // so we only do this the first time
        if (!executionState.hasBeenRun) {
            val commandsInFileSet = NewCommandsIndex.getAllKeys(LatexProjectStructure.getFilesetScopeFor(mainFile, environment.project))
            // Option 1 in http://mirrors.ctan.org/macros/latex/contrib/glossaries/glossariesbegin.pdf
            val usesTexForGlossaries = "\\makenoidxglossaries" in commandsInFileSet

            if (usesTexForGlossaries) {
                runConfig.compileTwice = true
            }

            // If no index package is used, we assume we won't have to run makeindex
            val includedPackages = ReadAction.compute<Set<LatexLib>, RuntimeException> {
                executionState.resolvedMainFile
                    ?.psiFile(runConfig.project)
                    ?.includedPackagesInFileset()
                    ?: setOf()
            }

            isMakeindexNeeded = includedPackages.intersect(PackageMagic.index + PackageMagic.glossary).isNotEmpty() && runConfig.compiler?.includesMakeindex == false && !usesTexForGlossaries

            // Some packages do handle makeindex themselves
            // Note that when you use imakeidx with the noautomatic option it won't, but we don't check for that
            if (includedPackages.contains(LatexLib.IMAKEIDX) && !LatexRunConfigurationStaticSupport.usesAuxilOrOutDirectory(runConfig)) {
                isMakeindexNeeded = false
            }
        }

        // Run makeindex when applicable
        if (executionState.isFirstRunConfig && (runConfig.makeindexRunConfigs.isNotEmpty() || isMakeindexNeeded)) {
            handler.addProcessListener(RunMakeindexListener(runConfig, environment, executionState))
        }

        return isMakeindexNeeded
    }

    private fun isLastCompile(isMakeindexNeeded: Boolean, handler: KillableProcessHandler): Boolean {
        val shouldCompileTwice = runConfig.compiler != LatexCompiler.LATEXMK && runConfig.compileTwice

        // If there is no bibtex/makeindex involved and we don't need to compile twice, then this is the last compile
        if (runConfig.bibRunConfigs.isEmpty() && !isMakeindexNeeded && runConfig.externalToolRunConfigs.isEmpty()) {
            if (!shouldCompileTwice) {
                executionState.markLastPass()
            }

            // Schedule the second compile only if this is the first compile
            if (!executionState.isLastRunConfig && shouldCompileTwice) {
                handler.addProcessListener(RunLatexListener(runConfig, environment, executionState))
                return false
            }
        }

        return true
    }

    private fun scheduleBibtexRunIfNeeded(handler: KillableProcessHandler) {
        runConfig.bibRunConfigs.forEachIndexed { index, bibSettings ->
            if (!executionState.isFirstRunConfig) {
                return@forEachIndexed
            }

            // Only run latex after the last one
            if (index == runConfig.bibRunConfigs.size - 1) {
                handler.addProcessListener(RunBibtexListener(bibSettings, runConfig, environment, executionState, true))
            }
            else {
                handler.addProcessListener(RunBibtexListener(bibSettings, runConfig, environment, executionState, false))
            }
        }
    }

    private fun schedulePdfViewerIfNeeded(handler: KillableProcessHandler) {
        // Do not schedule to open the pdf viewer when this is not the last run config in the chain or it is an auto compile
        if (executionState.isLastRunConfig && !runConfig.isAutoCompiling) {
            addOpenViewerListener(handler, runConfig.requireFocus)
        }
    }

    private fun scheduleFileCleanup(handler: KillableProcessHandler) {
        if (executionState.isLastRunConfig) {
            handler.addProcessListener(FileCleanupListener(executionState.filesToCleanUp, executionState.directoriesToDeleteIfEmpty))
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
            val commandList = ParametersListUtil.parse(commandString).toMutableList()
            val outputFilePath = executionState.resolvedOutputFilePath ?: return

            val containsPlaceholder = commandList.contains("{pdf}")

            if (containsPlaceholder) {
                // Replace placeholder
                for (i in 0 until commandList.size) {
                    if (commandList[i].contains("{pdf}")) {
                        commandList[i] = commandList[i].replace("{pdf}", outputFilePath)
                    }
                }
            }
            else {
                // If no placeholder was used, assume the path is the final argument
                commandList += outputFilePath
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
        val currentFilePath = editor?.document?.let { FileDocumentManager.getInstance().getFile(it)?.path }
            // Get the main file from the run configuration as a fallback.
            ?: executionState.resolvedMainFile?.path
            ?: return

        // Set the OpenViewerListener to execute when the compilation is done.
        handler.addProcessListener(OpenViewerListener(viewer, runConfig, currentFilePath, line, environment.project, focusAllowed))
    }
}
