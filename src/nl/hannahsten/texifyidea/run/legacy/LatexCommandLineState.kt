package nl.hannahsten.texifyidea.run.legacy

import com.intellij.execution.process.ProcessHandler
import com.intellij.execution.runners.ExecutionEnvironment
import com.intellij.openapi.util.SystemInfo
import nl.hannahsten.texifyidea.run.LatexRunConfiguration

open class LatexCommandLineState(environment: ExecutionEnvironment, private val runConfig: LatexRunConfiguration) {

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

//            handler.addProcessListener(OpenCustomPdfViewerListener(commandList.toTypedArray(), runConfig = runConfig))
        }
        // Do nothing if the user selected that they do not want a viewer to open.
//        else if (runConfig.pdfViewer == InternalPdfViewer.NONE) return
        // Sumatra does not support DVI
//        else if (runConfig.pdfViewer == InternalPdfViewer.SUMATRA && (runConfig.sumatraPath != null || isSumatraAvailable) && runConfig.options.outputFormat == LatexCompiler.OutputFormat.PDF) {
            // Open Sumatra after compilation & execute inverse search.
//            handler.addProcessListener(SumatraForwardSearchListener(runConfig, environment))
//        }
//        else if (runConfig.pdfViewer is ExternalPdfViewer ||
//                 runConfig.pdfViewer in listOf(InternalPdfViewer.EVINCE, InternalPdfViewer.OKULAR, InternalPdfViewer.ZATHURA, InternalPdfViewer.SKIM)) {
//            ViewerForwardSearch(runConfig.pdfViewer ?: InternalPdfViewer.NONE).execute(handler, runConfig, environment, focusAllowed)
//        }
        else if (SystemInfo.isMac) {
            // Open default system viewer, source: https://ss64.com/osx/open.html
            val commandList = arrayListOf("open", runConfig.outputFilePath)
            // Fail silently, otherwise users who have set up something themselves get an exception every time when this command fails
//            handler.addProcessListener(OpenCustomPdfViewerListener(commandList.toTypedArray(), failSilently = true, runConfig = runConfig))
        }
        else if (SystemInfo.isLinux) {
            // Open default system viewer using xdg-open, since this is available in almost all desktop environments
            val commandList = arrayListOf("xdg-open", runConfig.outputFilePath)
//            handler.addProcessListener(OpenCustomPdfViewerListener(commandList.toTypedArray(), failSilently = true, runConfig = runConfig))
        }
    }
}
