package nl.rubensten.texifyidea.run.evince

import com.intellij.execution.runners.ExecutionEnvironment
import nl.rubensten.texifyidea.TeXception
import nl.rubensten.texifyidea.psi.LatexEnvironment
import nl.rubensten.texifyidea.run.LatexRunConfiguration
import nl.rubensten.texifyidea.util.*
import org.jetbrains.concurrency.runAsync

/**
 * Provides forward search for Evince.
 */
class EvinceForwardSearch {

    /**
     * Execute forward search based on the given environment.
     */
    fun execute(runConfig: LatexRunConfiguration, environment: ExecutionEnvironment) {
        run {
            val psiFile = runConfig.mainFile?.psiFile(environment.project) ?: return@run
            val document = psiFile.document() ?: return@run
            val editor = psiFile.openedEditor() ?: return@run

            if (document != editor.document) {
                return@run
            }

            val line = document.getLineNumber(editor.caretOffset()) + 1

            runAsync {
                try {
                    // This will start Evince if it is not running yet
                    EvinceConversation.forwardSearch(pdfFilePath = runConfig.outputFilePath, sourceFilePath = psiFile.virtualFile.path, line = line)
                } catch (ignored: TeXception) {
                }
            }
        }
    }
}