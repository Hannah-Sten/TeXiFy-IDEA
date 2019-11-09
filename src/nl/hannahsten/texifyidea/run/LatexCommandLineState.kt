package nl.hannahsten.texifyidea.run

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
import nl.hannahsten.texifyidea.run.evince.EvinceForwardSearch
import nl.hannahsten.texifyidea.run.evince.isEvinceAvailable
import nl.hannahsten.texifyidea.run.sumatra.SumatraForwardSearch
import nl.hannahsten.texifyidea.run.sumatra.isSumatraAvailable
import nl.hannahsten.texifyidea.util.files.FileUtil
import nl.hannahsten.texifyidea.util.files.createExcludedDir
import nl.hannahsten.texifyidea.util.files.psiFile
import nl.hannahsten.texifyidea.util.files.referencedFileSet
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
        val command: List<String> = compiler.getCommand(runConfig, environment.project)
                ?: throw ExecutionException("Compile command could not be created.")

        createOutDirs(mainFile)

        val commandLine = GeneralCommandLine(command).withWorkDirectory(mainFile.parent.path)
        val handler: ProcessHandler = KillableProcessHandler(commandLine)

        // Reports exit code to run output window when command is terminated
        ProcessTerminatedListener.attach(handler, environment.project)

        // Some initial setup
        if (!runConfig.hasBeenRun) {
            // Only at this moment we know the user really wants to run the run configuration, so only now we do the expensive check of
            // checking for bibliography commands
            if (runConfig.bibRunConfig == null && !compiler.includesBibtex) {
                runConfig.generateBibRunConfig()
            }

            runConfig.hasBeenRun = true
        }

        // If there is no bibtex involved and we don't need to compile twice, then this is the last compile
        if (runConfig.bibRunConfig == null) {
            if (!runConfig.compileTwice) {
                runConfig.isLastRunConfig = true
            }

            // If we need to compile twice but we don't use bibtex, schedule the second compile if this is the first compile
            if (!runConfig.isLastRunConfig && runConfig.compileTwice) {
                handler.addProcessListener(RunLatexListener(runConfig, environment))
                return handler
            }
        }
        
        runConfig.bibRunConfig?.let {
            if (runConfig.isSkipBibtex) {
                return@let
            }

            // Pass necessary latex run configurations settings to the bibtex run configuration.
            (it.configuration as? BibtexRunConfiguration)?.apply {
                this.mainFile = mainFile
                // Check if the aux, out, or src folder should be used as bib working dir.
                when {
                    runConfig.hasAuxiliaryDirectories -> {
                        this.bibWorkingDir = ProjectRootManager.getInstance(project).fileIndex.getContentRootForFile(mainFile)?.findChild("auxil")
                    }
                    runConfig.hasOutputDirectories -> {
                        this.bibWorkingDir = ProjectRootManager.getInstance(project).fileIndex.getContentRootForFile(mainFile)?.findChild("out")
                    }
                    else -> {
                        this.bibWorkingDir = ProjectRootManager.getInstance(project).fileIndex.getContentRootForFile(mainFile)?.findChild(mainFile.parent.name)
                    }
                }
            }

            handler.addProcessListener(RunBibtexListener(it, runConfig, environment))

            // Skip the other handlers
            return handler
        }

        // Do not open the pdf viewer when this is not the last run config in the chain
        if (runConfig.isLastRunConfig) {
            openPdfViewer(handler)
        }

        return handler
    }

    /**
     * Add a certain process listener for opening the right pdf viewer depending on settings and OS.
     */
    private fun openPdfViewer(handler: ProcessHandler) {
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

            handler.addProcessListener(OpenPdfViewerListener(commandList.toTypedArray()))
        }
        else if (runConfig.sumatraPath != null || isSumatraAvailable) {
            // Open Sumatra after compilation & execute inverse search.
            SumatraForwardSearch().execute(handler, runConfig, environment)
        }
        else if(isEvinceAvailable()) {
            EvinceForwardSearch().execute(handler, runConfig, environment)
        }
        else if (SystemInfo.isMac) {
            // Open default system viewer, source: https://ss64.com/osx/open.html
            val commandList = arrayListOf("open", runConfig.outputFilePath)
            // Fail silently, otherwise users who have set up something themselves get an exception every time when this command fails
            handler.addProcessListener(OpenPdfViewerListener(commandList.toTypedArray(), failSilently = true))
        }
        else if (SystemInfo.isLinux) {
            // Open default system viewer using xdg-open, since this is available in almost all desktop environments
            val commandList = arrayListOf("xdg-open", runConfig.outputFilePath)
            handler.addProcessListener(OpenPdfViewerListener(commandList.toTypedArray(), failSilently = true))
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
