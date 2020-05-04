package nl.hannahsten.texifyidea.run.latex

import com.intellij.execution.ExecutionException
import com.intellij.execution.configurations.CommandLineState
import com.intellij.execution.configurations.GeneralCommandLine
import com.intellij.execution.process.KillableProcessHandler
import com.intellij.execution.process.ProcessHandler
import com.intellij.execution.process.ProcessTerminatedListener
import com.intellij.execution.runners.ExecutionEnvironment
import com.intellij.openapi.project.IndexNotReadyException
import com.intellij.openapi.roots.ProjectRootManager
import com.intellij.openapi.util.SystemInfo
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiFile
import nl.hannahsten.texifyidea.editor.autocompile.AutoCompileDoneListener
import nl.hannahsten.texifyidea.run.OpenCustomPdfViewerListener
import nl.hannahsten.texifyidea.run.bibtex.BibtexRunConfiguration
import nl.hannahsten.texifyidea.run.bibtex.RunBibtexListener
import nl.hannahsten.texifyidea.run.linuxpdfviewer.PdfViewer
import nl.hannahsten.texifyidea.run.linuxpdfviewer.ViewerForwardSearch
import nl.hannahsten.texifyidea.run.makeindex.RunMakeindexListener
import nl.hannahsten.texifyidea.run.sumatra.SumatraForwardSearchListener
import nl.hannahsten.texifyidea.run.sumatra.isSumatraAvailable
import nl.hannahsten.texifyidea.settings.TexifySettings
import nl.hannahsten.texifyidea.util.Magic.Package.index
import nl.hannahsten.texifyidea.util.files.FileUtil
import nl.hannahsten.texifyidea.util.files.createExcludedDir
import nl.hannahsten.texifyidea.util.files.psiFile
import nl.hannahsten.texifyidea.util.files.referencedFileSet
import nl.hannahsten.texifyidea.util.includedPackages
import java.io.File

/**
 * Run the run configuration: start the compile process and initiate forward search (when applicable).
 *
 * @author Sten Wessel
 */
open class LatexCommandLineState(environment: ExecutionEnvironment, private val runConfig: LatexRunConfiguration) : CommandLineState(environment) {

    @Throws(ExecutionException::class)
    override fun startProcess(): ProcessHandler {
        val compiler = runConfig.compiler ?: throw ExecutionException("No valid compiler specified.")
        val mainFile = runConfig.mainFile ?: throw ExecutionException("Main file is not specified.")

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

        // Make sure to create the command after generating the bib run config (which might change the output path)
        val command: List<String> = compiler.getCommand(runConfig, environment.project)
                ?: throw ExecutionException("Compile command could not be created.")

        createOutDirs(mainFile)

        val commandLine = GeneralCommandLine(command).withWorkDirectory(mainFile.parent.path)
                .withEnvironment(runConfig.environmentVariables.envs)
        val handler = KillableProcessHandler(commandLine)

        // Reports exit code to run output window when command is terminated
        ProcessTerminatedListener.attach(handler, environment.project)


        var isMakeindexNeeded = false

        // Run makeindex when applicable
        if (runConfig.isFirstRunConfig && (runConfig.makeindexRunConfig != null || !runConfig.hasBeenRun)) {
            // If no index package is used, we assume we won't have to run makeindex
            val includedPackages = runConfig.mainFile
                    ?.psiFile(runConfig.project)
                    ?.includedPackages()
                    ?: setOf()
            isMakeindexNeeded = includedPackages.intersect(index.asIterable()).isNotEmpty() && runConfig.compiler?.includesMakeindex == false

            if (isMakeindexNeeded) {
                // Some packages do handle makeindex themselves
                // Note that when you use imakeidx with the noautomatic option it won't, but we don't check for that
                if (!includedPackages.contains("imakeidx") || runConfig.usesAuxilOrOutDirectory()) {
                    handler.addProcessListener(RunMakeindexListener(runConfig, environment))
                }
            }
        }

        runConfig.hasBeenRun = true

        // If there is no bibtex/makeindex involved and we don't need to compile twice, then this is the last compile
        if (runConfig.bibRunConfigs.isEmpty() && !isMakeindexNeeded) {
            if (!runConfig.compileTwice) {
                runConfig.isLastRunConfig = true
            }

            // Schedule the second compile only if this is the first compile
            if (!runConfig.isLastRunConfig && runConfig.compileTwice) {
                handler.addProcessListener(RunLatexListener(runConfig, environment))
                return handler
            }
        }

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

        // Do not schedule to open the pdf viewer when this is not the last run config in the chain
        if (runConfig.isLastRunConfig) {
            addOpenViewerListener(handler, runConfig.allowFocusChange)
            handler.addProcessListener(AutoCompileDoneListener())
        }

        return handler
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
        else if (runConfig.sumatraPath != null || isSumatraAvailable) {
            // Open Sumatra after compilation & execute inverse search.
            handler.addProcessListener(SumatraForwardSearchListener(runConfig, environment))
        }
        else if (TexifySettings.getInstance().pdfViewer in listOf(PdfViewer.EVINCE, PdfViewer.OKULAR, PdfViewer.SKIM)) {
            ViewerForwardSearch(TexifySettings.getInstance().pdfViewer).execute(handler, runConfig, environment, focusAllowed)
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

    /**
     * Creates the output directories to place all produced files.
     */
    @Throws(ExecutionException::class)
    private fun createOutDirs(mainFile: VirtualFile) {
        val fileIndex = ProjectRootManager.getInstance(environment.project).fileIndex

        val includeRoot = mainFile.parent
        val parentPath = (fileIndex.getContentRootForFile(mainFile, false)?.path ?: includeRoot.path)
        val outPath = "$parentPath/out"

        // Create output path for mac
        val module = fileIndex.getModuleForFile(mainFile, false)
        module?.createExcludedDir(outPath) ?: File(outPath).mkdirs()

        val files: Set<PsiFile>
        try {
            files = mainFile.psiFile(environment.project)?.referencedFileSet() ?: emptySet()
        }
        catch (e: IndexNotReadyException) {
            throw ExecutionException("Please wait until the indices are built.", e)
        }

        // Create output paths for mac (see issue #70 on GitHub)
        files.asSequence()
                .mapNotNull { FileUtil.pathRelativeTo(includeRoot.path, it.virtualFile.parent.path) }
                .forEach { File(outPath + it).mkdirs() }
    }
}
