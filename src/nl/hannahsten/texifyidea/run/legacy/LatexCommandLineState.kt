package nl.hannahsten.texifyidea.run.legacy

import com.intellij.execution.ExecutionException
import com.intellij.execution.configurations.CommandLineState
import com.intellij.execution.configurations.GeneralCommandLine
import com.intellij.execution.process.KillableProcessHandler
import com.intellij.execution.process.ProcessHandler
import com.intellij.execution.process.ProcessTerminatedListener
import com.intellij.execution.runners.ExecutionEnvironment
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.SystemInfo
import com.intellij.openapi.vfs.VirtualFile
import nl.hannahsten.texifyidea.editor.autocompile.AutoCompileDoneListener
import nl.hannahsten.texifyidea.lang.LatexPackage
import nl.hannahsten.texifyidea.lang.commands.LatexGenericRegularCommand
import nl.hannahsten.texifyidea.run.FileCleanupListener
import nl.hannahsten.texifyidea.run.pdfviewer.OpenCustomPdfViewerListener
import nl.hannahsten.texifyidea.run.legacy.bibtex.BibtexRunConfiguration
import nl.hannahsten.texifyidea.run.legacy.bibtex.RunBibtexListener
import nl.hannahsten.texifyidea.run.compiler.latex.LatexCompiler
import nl.hannahsten.texifyidea.run.LatexRunConfiguration
import nl.hannahsten.texifyidea.run.legacy.externaltool.RunExternalToolListener
import nl.hannahsten.texifyidea.run.pdfviewer.linuxpdfviewer.InternalPdfViewer
import nl.hannahsten.texifyidea.run.pdfviewer.linuxpdfviewer.ViewerForwardSearch
import nl.hannahsten.texifyidea.run.legacy.makeindex.RunMakeindexListener
import nl.hannahsten.texifyidea.run.pdfviewer.ExternalPdfViewer
import nl.hannahsten.texifyidea.run.step.LatexCompileStepProvider
import nl.hannahsten.texifyidea.run.pdfviewer.sumatra.SumatraForwardSearchListener
import nl.hannahsten.texifyidea.run.pdfviewer.sumatra.isSumatraAvailable
import nl.hannahsten.texifyidea.util.files.commandsInFileSet
import nl.hannahsten.texifyidea.util.files.psiFile
import nl.hannahsten.texifyidea.util.includedPackages
import nl.hannahsten.texifyidea.util.magic.PackageMagic
import java.io.File

/**
 * Run the run configuration: start the compile process and initiate forward search (when applicable).
 *
 * @author Sten Wessel
 */
open class LatexCommandLineState(environment: ExecutionEnvironment, private val runConfig: LatexRunConfiguration) : CommandLineState(environment) {

    @Throws(ExecutionException::class)
    override fun startProcess(): ProcessHandler {
        val compiler = runConfig.getConfigOptions().compiler ?: throw ExecutionException("No valid compiler specified.")
        val mainFile = runConfig.mainFile ?: throw ExecutionException("Main file is not specified.")

        // If the outdirs do not exist, we assume this is because either something went wrong and an incorrect output path was filled in,
        // or the user did not create a new project, for example by opening or importing existing resources,
        // so they still need to be created.
        if (runConfig.outputPath.virtualFile == null) {
            runConfig.outputPath.getAndCreatePath()
        }

        firstRunSetup(compiler)
        if (!runConfig.getLatexDistributionType().isMiktex()) {
            runConfig.outputPath.updateOutputSubDirs()
        }

        val handler = createHandler(mainFile, compiler)
        val isMakeindexNeeded = runMakeindexIfNeeded(handler, mainFile, runConfig.filesToCleanUp)
        val isAnyExternalToolNeeded = runExternalToolsIfNeeded(handler, mainFile, runConfig.project)
        runConfig.hasBeenRun = true

        if (!isLastCompile(isMakeindexNeeded, isAnyExternalToolNeeded, handler)) return handler
        scheduleBibtexRunIfNeeded(handler)
        schedulePdfViewerIfNeeded(handler)
        scheduleFileCleanup(runConfig.filesToCleanUp, handler)

        return handler
    }

    private fun createHandler(mainFile: VirtualFile, compiler: LatexCompiler): KillableProcessHandler {
        // Make sure to create the command after generating the bib run config (which might change the output path)
        val command: List<String> = compiler.getCommand(LatexCompileStepProvider.createStep(runConfig))
            ?: throw ExecutionException("Compile command could not be created.")

        val commandLine = GeneralCommandLine(command).withWorkDirectory(mainFile.parent.path)
            .withEnvironment(runConfig.environmentVariables.envs)
        val handler = KillableProcessHandler(commandLine)

        // Reports exit code to run output window when command is terminated
        ProcessTerminatedListener.attach(handler, environment.project)

        return handler
    }

    private fun firstRunSetup(compiler: LatexCompiler) {
        // Some initial setup
        if (!runConfig.hasBeenRun) {
            // Only at this moment we know the user really wants to run the run configuration, so only now we do the expensive check of
            // checking for bibliography commands
            if (runConfig.bibRunConfigs.isEmpty() && !compiler.includesBibtex) {
                runConfig.generateBibRunConfig()

                runConfig.bibRunConfigs.forEach {
                    val bibSettings = it

                    // Pass necessary latex run configurations settings to the bibtex run configuration.
                    (bibSettings.configuration as? BibtexRunConfiguration)?.apply {
                        // Check if the aux, out, or src folder should be used as bib working dir.
                        this.bibWorkingDir = runConfig.getAuxilDirectory()
                    }
                }
            }
        }
    }

    private fun runExternalToolsIfNeeded(
        handler: KillableProcessHandler,
        mainFile: VirtualFile,
        project: Project
    ): Boolean {
        val isAnyExternalToolNeeded = RunExternalToolListener.getRequiredExternalTools(mainFile, project).isNotEmpty()

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
            val commandsInFileSet = mainFile.psiFile(environment.project)?.commandsInFileSet()?.mapNotNull { it.name } ?: emptyList()

            // Option 1 in http://mirrors.ctan.org/macros/latex/contrib/glossaries/glossariesbegin.pdf
            val usesTexForGlossaries = "\\" + LatexGenericRegularCommand.MAKENOIDXGLOSSARIES.commandWithSlash in commandsInFileSet

            if (usesTexForGlossaries) {
                runConfig.compileTwice = true
            }

            // If no index package is used, we assume we won't have to run makeindex
            val includedPackages = runConfig.mainFile
                ?.psiFile(runConfig.project)
                ?.includedPackages()
                ?: setOf()

            isMakeindexNeeded = includedPackages.intersect(PackageMagic.index + PackageMagic.glossary)
                .isNotEmpty() && runConfig.getConfigOptions().compiler?.includesMakeindex == false && !usesTexForGlossaries

            // Some packages do handle makeindex themselves
            // Note that when you use imakeidx with the noautomatic option it won't, but we don't check for that
            if (includedPackages.contains(LatexPackage.IMAKEIDX) && !runConfig.usesAuxilOrOutDirectory()) {
                isMakeindexNeeded = false
            }
        }

        // Run makeindex when applicable
        if (runConfig.isFirstRunConfig && (runConfig.makeindexRunConfigs.isNotEmpty() || isMakeindexNeeded)) {
            handler.addProcessListener(RunMakeindexListener(runConfig, environment, filesToCleanUp))
        }

        return isMakeindexNeeded
    }

    private fun isLastCompile(isMakeindexNeeded: Boolean, isAnyExternalToolNeeded: Boolean, handler: KillableProcessHandler): Boolean {
        // If there is no bibtex/makeindex involved and we don't need to compile twice, then this is the last compile
        if (runConfig.bibRunConfigs.isEmpty() && !isMakeindexNeeded && !isAnyExternalToolNeeded) {
            if (!runConfig.compileTwice) {
                runConfig.isLastRunConfig = true
            }

            // Schedule the second compile only if this is the first compile
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
        // Do not schedule to open the pdf viewer when this is not the last run config in the chain
        if (runConfig.isLastRunConfig) {
            addOpenViewerListener(handler, runConfig.allowFocusChange)
            handler.addProcessListener(AutoCompileDoneListener())
        }
    }

    private fun scheduleFileCleanup(filesToCleanUp: MutableList<File>, handler: KillableProcessHandler) {
        if (runConfig.isLastRunConfig) {
            handler.addProcessListener(FileCleanupListener(filesToCleanUp))
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
            else if (!containsPlaceholder) {
                // If no placeholder was used, assume the path is the final argument
                commandList += runConfig.outputFilePath
            }

            handler.addProcessListener(OpenCustomPdfViewerListener(commandList.toTypedArray(), runConfig = runConfig))
        }
        // Do nothing if the user selected that they do not want a viewer to open.
        else if (runConfig.pdfViewer == InternalPdfViewer.NONE) return
        // Sumatra does not support DVI
        else if (runConfig.pdfViewer == InternalPdfViewer.SUMATRA && (runConfig.sumatraPath != null || isSumatraAvailable) && runConfig.outputFormat == LatexCompiler.OutputFormat.PDF) {
            // Open Sumatra after compilation & execute inverse search.
            handler.addProcessListener(SumatraForwardSearchListener(runConfig, environment))
        }
        else if (runConfig.pdfViewer is ExternalPdfViewer ||
                 runConfig.pdfViewer in listOf(InternalPdfViewer.EVINCE, InternalPdfViewer.OKULAR, InternalPdfViewer.ZATHURA, InternalPdfViewer.SKIM)) {
            ViewerForwardSearch(runConfig.pdfViewer ?: InternalPdfViewer.NONE).execute(handler, runConfig, environment, focusAllowed)
        }
        else if (SystemInfo.isMac) {
            // Open default system viewer, source: https://ss64.com/osx/open.html
            val commandList = arrayListOf("open", runConfig.outputFilePath)
            // Fail silently, otherwise users who have set up something themselves get an exception every time when this command fails
            handler.addProcessListener(OpenCustomPdfViewerListener(commandList.toTypedArray(), failSilently = true, runConfig = runConfig))
        }
        else if (SystemInfo.isLinux) {
            // Open default system viewer using xdg-open, since this is available in almost all desktop environments
            val commandList = arrayListOf("xdg-open", runConfig.outputFilePath)
            handler.addProcessListener(OpenCustomPdfViewerListener(commandList.toTypedArray(), failSilently = true, runConfig = runConfig))
        }
    }
}
