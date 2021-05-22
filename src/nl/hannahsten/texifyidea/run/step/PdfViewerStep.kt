package nl.hannahsten.texifyidea.run.step

import com.intellij.execution.process.*
import com.intellij.notification.Notification
import com.intellij.notification.NotificationType
import com.intellij.openapi.application.invokeLater
import com.intellij.openapi.application.runInEdt
import com.intellij.openapi.application.runReadAction
import com.intellij.openapi.util.SystemInfo
import nl.hannahsten.texifyidea.TeXception
import nl.hannahsten.texifyidea.action.ForwardSearchAction
import nl.hannahsten.texifyidea.psi.LatexEnvironment
import nl.hannahsten.texifyidea.run.LatexRunConfiguration
import nl.hannahsten.texifyidea.run.StepExecutionHandler
import nl.hannahsten.texifyidea.run.compiler.latex.LatexCompiler
import nl.hannahsten.texifyidea.run.pdfviewer.ExternalPdfViewer
import nl.hannahsten.texifyidea.run.pdfviewer.OpenCustomPdfViewerListener
import nl.hannahsten.texifyidea.run.pdfviewer.PdfViewer
import nl.hannahsten.texifyidea.run.pdfviewer.linuxpdfviewer.InternalPdfViewer
import nl.hannahsten.texifyidea.run.pdfviewer.linuxpdfviewer.ViewerForwardSearch
import nl.hannahsten.texifyidea.run.pdfviewer.sumatra.SumatraConversation
import nl.hannahsten.texifyidea.run.pdfviewer.sumatra.SumatraForwardSearchListener
import nl.hannahsten.texifyidea.run.pdfviewer.sumatra.isSumatraAvailable
import nl.hannahsten.texifyidea.run.ui.console.LatexExecutionConsole
import nl.hannahsten.texifyidea.util.caretOffset
import nl.hannahsten.texifyidea.util.currentTextEditor
import nl.hannahsten.texifyidea.util.files.document
import nl.hannahsten.texifyidea.util.files.isRoot
import nl.hannahsten.texifyidea.util.files.openedEditor
import nl.hannahsten.texifyidea.util.files.psiFile
import nl.hannahsten.texifyidea.util.name
import nl.hannahsten.texifyidea.util.parentOfType
import org.jetbrains.concurrency.runAsync
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.io.OutputStream

class PdfViewerStep(override val provider: StepProvider, override val configuration: LatexRunConfiguration) : Step {

    val pdfViewer: PdfViewer = InternalPdfViewer.EVINCE

    override fun configure() {
        TODO("Not yet implemented")
    }

    override fun execute(id: String, console: LatexExecutionConsole): ProcessHandler {
        return object : ProcessHandler() {
            override fun destroyProcessImpl() {
                notifyProcessTerminated(0)
            }

            override fun detachProcessImpl() {
                notifyProcessDetached()
            }

            override fun detachIsDefault(): Boolean {
                return false
            }

            override fun getProcessInput(): OutputStream? {
                return null
            }

            override fun startNotify() {
                super.startNotify()
                runInEdt {
                    // TODO open pdf file in viewer if not open
                    val currentEditor = configuration.project.currentTextEditor() ?: return@runInEdt
                    val currentFile = currentEditor.file ?: return@runInEdt
                    ForwardSearchAction(pdfViewer).actionPerformed(currentFile, configuration.project, currentEditor)
                }
                super.notifyProcessTerminated(0)
            }
        }
    }

    private fun openViewer() {
        // First check if the user specified a custom viewer, if not then try other supported viewers
        if (!configuration.viewerCommand.isNullOrEmpty()) {

            // Split user command on spaces, then replace {pdf} if needed
            val commandString = configuration.viewerCommand!!

            // Split on spaces
            val commandList = commandString.split(" ").toMutableList()

            val containsPlaceholder = commandList.contains("{pdf}")

            if (containsPlaceholder) {
                // Replace placeholder
                for (i in 0 until commandList.size) {
                    if (commandList[i].contains("{pdf}")) {
                        commandList[i] = commandList[i].replace("{pdf}", configuration.outputFilePath)
                    }
                }
            }
            else if (!containsPlaceholder) {
                // If no placeholder was used, assume the path is the final argument
                commandList += configuration.outputFilePath
            }

            openCustomPdfViewer(commandList.toTypedArray(), runConfig = configuration)
        }
        // Do nothing if the user selected that they do not want a viewer to open.
        else if (configuration.pdfViewer == InternalPdfViewer.NONE) return
        // Sumatra does not support DVI
        else if (configuration.pdfViewer == InternalPdfViewer.SUMATRA && (configuration.sumatraPath != null || isSumatraAvailable) && configuration.outputFormat == LatexCompiler.OutputFormat.PDF) {
            // Open Sumatra after compilation & execute inverse search.
            SumatraForwardSearchListener(configuration)
        }
        else if (configuration.pdfViewer is ExternalPdfViewer ||
            configuration.pdfViewer in listOf(
                InternalPdfViewer.EVINCE,
                InternalPdfViewer.OKULAR,
                InternalPdfViewer.ZATHURA,
                InternalPdfViewer.SKIM
            )
        ) {
            ViewerForwardSearch(configuration.pdfViewer ?: InternalPdfViewer.NONE).execute(configuration)
        }
        else if (SystemInfo.isMac) {
            // Open default system viewer, source: https://ss64.com/osx/open.html
            val commandList = arrayListOf("open", configuration.outputFilePath)
            // Fail silently, otherwise users who have set up something themselves get an exception every time when this command fails
            openCustomPdfViewer(commandList.toTypedArray(), failSilently = true, runConfig = configuration)
        }
        else if (SystemInfo.isLinux) {
            // Open default system viewer using xdg-open, since this is available in almost all desktop environments
            val commandList = arrayListOf("xdg-open", configuration.outputFilePath)
            openCustomPdfViewer(commandList.toTypedArray(), failSilently = true, runConfig = configuration)
        }
    }

    private fun openCustomPdfViewer(command: Array<String>, failSilently: Boolean = true, runConfig: LatexRunConfiguration) {
        try {
            ProcessBuilder(*command).start()
        }
        catch (e: IOException) {
            if (!failSilently) {
                // Probably user error
                Notification(
                    "LaTeX",
                    "Could not open pdf file",
                    "An error occured when trying to open the pdf using ${command.joinToString(" ")} with message ${e.message}",
                    NotificationType.ERROR
                ).notify(runConfig.project)
            }
        }
    }

    private fun openSumatra(runConfig: LatexRunConfiguration) {
        // First check if the user provided a custom path to SumatraPDF, if not, check if it is installed
        try {
            SumatraConversation.openFile(runConfig.outputFilePath, sumatraPath = runConfig.sumatraPath)
        }
        catch (ignored: TeXception) {
        }

        // Forward search.
        invokeLater {
            val psiFile = runConfig.mainFile?.psiFile(runConfig.project) ?: return@invokeLater
            val document = psiFile.document() ?: return@invokeLater

            val editor = psiFile.openedEditor() ?: return@invokeLater

            if (document != editor.document) {
                return@invokeLater
            }

            // Do not do forward search when editing the preamble.
            if (psiFile.isRoot()) {
                val element = psiFile.findElementAt(editor.caretOffset()) ?: return@invokeLater
                val latexEnvironment = element.parentOfType(LatexEnvironment::class) ?: return@invokeLater
                if (latexEnvironment.name()?.text != "document") {
                    return@invokeLater
                }
            }

            val line = document.getLineNumber(editor.caretOffset()) + 1

            runAsync {
                try {
                    // Wait for sumatra pdf to start. 1250ms should be plenty.
                    // Otherwise the person is out of luck ¯\_(ツ)_/¯
                    Thread.sleep(1250)
                    // Never focus, because forward search will work fine without focus, and the user might want to continue typing after doing forward search/compiling
                    SumatraConversation.forwardSearch(
                        sourceFilePath = psiFile.virtualFile.path,
                        line = line,
                        focus = false
                    )
                }
                catch (ignored: TeXception) {
                }
            }
        }
    }

    /**
     * Add a certain process listener for opening the right pdf viewer depending on settings and OS.
     *
     * @param focusAllowed Whether focussing the pdf viewer is allowed. If not, it may happen forward search is not executed (in case the pdf viewer does not support forward search without changing focus).
     */
//    private fun addOpenViewerListener(handler: PdfViewerExecutionHandler) {
//        // First check if the user specified a custom viewer, if not then try other supported viewers
//        if (!configuration.viewerCommand.isNullOrEmpty()) {
//
//            // Split user command on spaces, then replace {pdf} if needed
//            val commandString = configuration.viewerCommand!!
//
//            // Split on spaces
//            val commandList = commandString.split(" ").toMutableList()
//
//            val containsPlaceholder = commandList.contains("{pdf}")
//
//            if (containsPlaceholder) {
//                // Replace placeholder
//                for (i in 0 until commandList.size) {
//                    if (commandList[i].contains("{pdf}")) {
//                        commandList[i] = commandList[i].replace("{pdf}", configuration.outputFilePath)
//                    }
//                }
//            }
//            else if (!containsPlaceholder) {
//                // If no placeholder was used, assume the path is the final argument
//                commandList += configuration.outputFilePath
//            }
//
//            handler.addProcessListener(
//                OpenCustomPdfViewerListener(
//                    commandList.toTypedArray(),
//                    runConfig = configuration
//                )
//            )
//        }
//        // Do nothing if the user selected that they do not want a viewer to open.
//        else if (configuration.pdfViewer == InternalPdfViewer.NONE) return
//        // Sumatra does not support DVI
//        else if (configuration.pdfViewer == InternalPdfViewer.SUMATRA && (configuration.sumatraPath != null || isSumatraAvailable) && configuration.outputFormat == LatexCompiler.OutputFormat.PDF) {
//            // Open Sumatra after compilation & execute inverse search.
//            handler.addProcessListener(SumatraForwardSearchListener(configuration))
//        }
//        else if (configuration.pdfViewer is ExternalPdfViewer ||
//            configuration.pdfViewer in listOf(
//                InternalPdfViewer.EVINCE,
//                InternalPdfViewer.OKULAR,
//                InternalPdfViewer.ZATHURA,
//                InternalPdfViewer.SKIM
//            )
//        ) {
//            ViewerForwardSearch(configuration.pdfViewer ?: InternalPdfViewer.NONE).execute(handler, configuration)
//        }
//        else if (SystemInfo.isMac) {
//            // Open default system viewer, source: https://ss64.com/osx/open.html
//            val commandList = arrayListOf("open", configuration.outputFilePath)
//            // Fail silently, otherwise users who have set up something themselves get an exception every time when this command fails
//            handler.addProcessListener(
//                OpenCustomPdfViewerListener(
//                    commandList.toTypedArray(),
//                    failSilently = true,
//                    runConfig = configuration
//                )
//            )
//        }
//        else if (SystemInfo.isLinux) {
//            // Open default system viewer using xdg-open, since this is available in almost all desktop environments
//            val commandList = arrayListOf("xdg-open", configuration.outputFilePath)
//            handler.addProcessListener(
//                OpenCustomPdfViewerListener(
//                    commandList.toTypedArray(),
//                    failSilently = true,
//                    runConfig = configuration
//                )
//            )
//        }
//    }
}