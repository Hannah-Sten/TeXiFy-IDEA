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
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiFile
import nl.rubensten.texifyidea.TeXception
import nl.rubensten.texifyidea.psi.LatexEnvironment
import nl.rubensten.texifyidea.run.evince.isEvinceAvailable
import nl.rubensten.texifyidea.util.*
import org.jetbrains.concurrency.runAsync
import java.io.File

/**
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

        runConfig.bibRunConfig?.let {
            if (runConfig.isSkipBibtex) {
                return@let
            }

            // Pass necessary latex run configurations settings to the bibtex run configuration.
            (it.configuration as? BibtexRunConfiguration)?.apply {
                this.mainFile = mainFile
                // Check if the aux, out, or src folder should be used as bib working dir.
                when {
                    runConfig.hasAuxiliaryDirectories() -> {
                        this.bibWorkingDir = ProjectRootManager.getInstance(project).fileIndex.getContentRootForFile(mainFile)?.findChild("auxil")
                    }
                    runConfig.hasOutputDirectories() -> {
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

        // Open Sumatra after compilation & execute inverse search.
        if (isSumatraAvailable) {
            handler.addProcessListener(OpenSumatraListener(runConfig))

            // Inverse search.
            run {
                val psiFile = runConfig.mainFile.psiFile(environment.project) ?: return@run
                val document = psiFile.document() ?: return@run
                val editor = psiFile.openedEditor() ?: return@run

                if (document != editor.document) {
                    return@run
                }

                // Do not do inverse search when editing the preamble.
                if (psiFile.isRoot()) {
                    val element = psiFile.findElementAt(editor.caretOffset()) ?: return@run
                    val environment = element.parentOfType(LatexEnvironment::class) ?: return@run
                    if (environment.name()?.text != "document") {
                        return@run
                    }
                }

                val line = document.getLineNumber(editor.caretOffset()) + 1

                runAsync {
                    try {
                        // Wait for sumatra pdf to start. 1250ms should be plenty.
                        // Otherwise the person is out of luck ¯\_(ツ)_/¯
                        Thread.sleep(1250)
                        SumatraConversation.forwardSearch(sourceFilePath = psiFile.virtualFile.path, line = line)
                    }
                    catch (ignored: TeXception) {
                    }
                }
            }
        }

        if(isEvinceAvailable()) {

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
