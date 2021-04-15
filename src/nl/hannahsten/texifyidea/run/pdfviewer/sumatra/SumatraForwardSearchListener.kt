package nl.hannahsten.texifyidea.run.pdfviewer.sumatra

import com.intellij.execution.process.ProcessEvent
import com.intellij.execution.process.ProcessListener
import com.intellij.execution.runners.ExecutionEnvironment
import com.intellij.openapi.application.invokeLater
import com.intellij.openapi.util.Key
import nl.hannahsten.texifyidea.TeXception
import nl.hannahsten.texifyidea.psi.LatexEnvironment
import nl.hannahsten.texifyidea.run.LatexRunConfiguration
import nl.hannahsten.texifyidea.util.caretOffset
import nl.hannahsten.texifyidea.util.files.document
import nl.hannahsten.texifyidea.util.files.isRoot
import nl.hannahsten.texifyidea.util.files.openedEditor
import nl.hannahsten.texifyidea.util.files.psiFile
import nl.hannahsten.texifyidea.util.name
import nl.hannahsten.texifyidea.util.parentOfType
import org.jetbrains.concurrency.runAsync

/**
 * @author Sten Wessel
 */
class SumatraForwardSearchListener(
    val runConfig: LatexRunConfiguration,
    private val executionEnvironment: ExecutionEnvironment
) : ProcessListener {

    override fun processTerminated(event: ProcessEvent) {
        // First check if the user provided a custom path to SumatraPDF, if not, check if it is installed
        if (event.exitCode == 0 && (runConfig.sumatraPath != null || isSumatraAvailable)) {
            try {
                SumatraConversation.openFile(runConfig.outputFilePath, sumatraPath = runConfig.sumatraPath)
            }
            catch (ignored: TeXception) {
            }
        }

        // Forward search.
        invokeLater {
            val psiFile = runConfig.mainFile?.psiFile(executionEnvironment.project) ?: return@invokeLater
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
                    SumatraConversation.forwardSearch(sourceFilePath = psiFile.virtualFile.path, line = line, focus = false)
                }
                catch (ignored: TeXception) {
                }
            }
        }

        // Reset to default
        runConfig.allowFocusChange = true
    }

    override fun onTextAvailable(p0: ProcessEvent, p1: Key<*>) {
        // Do nothing.
    }

    override fun processWillTerminate(p0: ProcessEvent, p1: Boolean) {
        // Do nothing.
    }

    override fun startNotified(p0: ProcessEvent) {
        // Do nothing.
    }
}
