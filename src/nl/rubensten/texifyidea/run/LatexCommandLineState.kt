package nl.rubensten.texifyidea.run

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
import nl.rubensten.texifyidea.TeXception
import nl.rubensten.texifyidea.psi.LatexEnvironment
import nl.rubensten.texifyidea.run.compiler.BibliographyCompiler
import nl.rubensten.texifyidea.run.evince.EvinceForwardSearch
import nl.rubensten.texifyidea.run.evince.isEvinceAvailable
import nl.rubensten.texifyidea.run.sumatra.OpenSumatraListener
import nl.rubensten.texifyidea.run.sumatra.SumatraConversation
import nl.rubensten.texifyidea.run.sumatra.SumatraForwardSearch
import nl.rubensten.texifyidea.run.sumatra.isSumatraAvailable
import nl.rubensten.texifyidea.util.*
import org.jetbrains.concurrency.runAsync
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

        // Only at this moment we know the user really wants to run the run configuration, so only now we do the expensive check of
        // checking for bibliography commands
        if (runConfig.bibRunConfig == null) {
            if (runConfig.psiFile?.hasBibliography() == true) {
                runConfig.generateBibRunConfig(BibliographyCompiler.BIBTEX)
            } else if (runConfig.psiFile?.usesBiber() == true) {
                runConfig.generateBibRunConfig(BibliographyCompiler.BIBER)
            }
        }

        createOutDirs(mainFile)

        val commandLine = GeneralCommandLine(command).withWorkDirectory(mainFile.parent.path)
        val handler: ProcessHandler = KillableProcessHandler(commandLine)

        // Reports exit code to run output window when command is terminated
        ProcessTerminatedListener.attach(handler, environment.project)

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
        if (!runConfig.isLastRunConfig) {
            return handler
        }

        // First check if the user specified a custom viewer, if not then try other supported viewers
        if (!runConfig.viewerCommand.isNullOrEmpty()) {

            // Split user command on spaces, then replace {pdf} if needed?
            val commandString = runConfig.viewerCommand!!

            // Split on spaces
            val commandList = commandString.split(" ")

            // Replace placeholder
            var containsPlaceholder = false
            val mappedList = commandList.map {
                if (it.contains("{pdf}")) {
                    containsPlaceholder = true
                    val replacement: String = it.replace("{pdf}", runConfig.outputFilePath)
                    replacement
                }
                else {
                    it
                }
            }.toMutableList()

            // If no placeholder was used, append path to the command
            if (!containsPlaceholder) {
                mappedList += runConfig.outputFilePath
            }

            handler.addProcessListener(OpenPdfViewerListener(mappedList.toTypedArray()))
        }
        else if (runConfig.sumatraPath != null || isSumatraAvailable) {
            // Open Sumatra after compilation & execute inverse search.
            SumatraForwardSearch().execute(handler, runConfig, environment)
        }
        else if(isEvinceAvailable()) {
            EvinceForwardSearch().execute(runConfig, environment)
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

        return handler
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
