package nl.rubensten.texifyidea.run

import com.intellij.execution.process.ProcessEvent
import com.intellij.execution.process.ProcessListener
import com.intellij.openapi.util.Key
import nl.rubensten.texifyidea.TeXception

/**
 * @author Sten Wessel
 */
class OpenSumatraListener(val runConfig: LatexRunConfiguration) : ProcessListener {

    override fun processTerminated(event: ProcessEvent) {
        if (event.exitCode == 0 && isSumatraAvailable) {
            try {
                SumatraConversation.openFile(runConfig.outputFilePath, start = true)
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
