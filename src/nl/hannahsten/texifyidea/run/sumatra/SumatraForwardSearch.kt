package nl.hannahsten.texifyidea.run.sumatra

import com.intellij.execution.process.ProcessHandler
import com.intellij.execution.runners.ExecutionEnvironment
import nl.hannahsten.texifyidea.TeXception
import nl.hannahsten.texifyidea.psi.LatexEnvironment
import nl.hannahsten.texifyidea.run.latex.LatexRunConfiguration
import nl.hannahsten.texifyidea.util.caretOffset
import nl.hannahsten.texifyidea.util.files.document
import nl.hannahsten.texifyidea.util.files.isRoot
import nl.hannahsten.texifyidea.util.files.openedEditor
import nl.hannahsten.texifyidea.util.files.psiFile
import nl.hannahsten.texifyidea.util.name
import nl.hannahsten.texifyidea.util.parentOfType
import org.jetbrains.concurrency.runAsync

/**
 * Provides forward search for SumatraPDF.
 */
class SumatraForwardSearch {

    /**
     * Execute forward search based on the given environment.
     */
    fun execute(handler: ProcessHandler, runConfig: LatexRunConfiguration, executionEnvironment: ExecutionEnvironment, focusAllowed: Boolean = true) {
        handler.addProcessListener(OpenSumatraListener(runConfig))

        // Forward search.
        run {
            val psiFile = runConfig.mainFile?.psiFile(executionEnvironment.project) ?: return@run
            val document = psiFile.document() ?: return@run
            val editor = psiFile.openedEditor() ?: return@run

            if (document != editor.document) {
                return@run
            }

            // Do not do forward search when editing the preamble.
            if (psiFile.isRoot()) {
                val element = psiFile.findElementAt(editor.caretOffset()) ?: return@run
                val latexEnvironment = element.parentOfType(LatexEnvironment::class) ?: return@run
                if (latexEnvironment.name()?.text != "document") {
                    return@run
                }
            }

            val line = document.getLineNumber(editor.caretOffset()) + 1

            runAsync {
                try {
                    // Wait for sumatra pdf to start. 1250ms should be plenty.
                    // Otherwise the person is out of luck ¯\_(ツ)_/¯
                    Thread.sleep(1250)
                    SumatraConversation.forwardSearch(sourceFilePath = psiFile.virtualFile.path, line = line, focus = focusAllowed)
                }
                catch (ignored: TeXception) {
                }
            }
        }
    }
}