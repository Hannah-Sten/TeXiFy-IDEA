package nl.rubensten.texifyidea.run.evince

import com.intellij.execution.process.ProcessEvent
import com.intellij.execution.process.ProcessListener
import com.intellij.execution.runners.ExecutionEnvironment
import com.intellij.openapi.util.Key
import com.intellij.psi.PsiFile
import nl.rubensten.texifyidea.TeXception
import nl.rubensten.texifyidea.run.LatexRunConfiguration
import org.jetbrains.concurrency.runAsync

/**
 * Execute a forward search with Evince after the compilation is done.
 */
class OpenEvinceListener(val runConfig: LatexRunConfiguration, val environment: ExecutionEnvironment, val psiFile: PsiFile, val line: Int) : ProcessListener {

    override fun processTerminated(event: ProcessEvent) {
        if (event.exitCode == 0) {
            runAsync {
                try {
                    // This will start Evince if it is not running yet
                    EvinceConversation.forwardSearch(pdfFilePath = runConfig.outputFilePath, sourceFilePath = psiFile.virtualFile.path, line = line)
                }
                catch (ignored: TeXception) {
                }
            }
        }
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