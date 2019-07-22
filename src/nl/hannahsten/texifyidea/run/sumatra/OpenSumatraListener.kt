package nl.hannahsten.texifyidea.run.sumatra

import com.intellij.execution.process.ProcessEvent
import com.intellij.execution.process.ProcessListener
import com.intellij.openapi.util.Key
import nl.hannahsten.texifyidea.TeXception
import nl.hannahsten.texifyidea.run.latex.LatexRunConfiguration

/**
 * @author Sten Wessel
 */
class OpenSumatraListener(val runConfig: LatexRunConfiguration) : ProcessListener {

    override fun processTerminated(event: ProcessEvent) {
        // First check if the user provided a custom path to SumatraPDF, if not, check if it is installed
        if (event.exitCode == 0 && (runConfig.sumatraPath != null || isSumatraAvailable)) {
            try {
                SumatraConversation.openFile(runConfig.outputFilePath, sumatraPath = runConfig.sumatraPath)
            }
            catch (ignored: TeXception) {
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
