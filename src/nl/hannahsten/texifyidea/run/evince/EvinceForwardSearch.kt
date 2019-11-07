package nl.hannahsten.texifyidea.run.evince

import com.intellij.execution.process.ProcessHandler
import com.intellij.execution.runners.ExecutionEnvironment
import nl.hannahsten.texifyidea.run.LatexRunConfiguration
import nl.hannahsten.texifyidea.util.caretOffset
import nl.hannahsten.texifyidea.util.files.openedEditor
import nl.hannahsten.texifyidea.util.files.psiFile

/**
 * Provides forward search for Evince.
 */
class EvinceForwardSearch {

    /**
     * Execute forward search when the process is done.
     */
    fun execute(handler: ProcessHandler, runConfig: LatexRunConfiguration, environment: ExecutionEnvironment) {
        // We have to find the file and line number before scheduling the forward search
        val mainPsiFile = runConfig.mainFile?.psiFile(environment.project) ?: return
        val editor = mainPsiFile.openedEditor() ?: return

        // Get the line number in the currently open file
        val line = editor.document.getLineNumber(editor.caretOffset()) + 1

        // Get the currently open file to use for forward search
        val currentPsiFile = editor.document.psiFile(environment.project) ?: return

        // Set the OpenEvinceListener to execute when the compilation is done
        handler.addProcessListener(OpenEvinceListener(runConfig, environment, currentPsiFile, line))
    }
}