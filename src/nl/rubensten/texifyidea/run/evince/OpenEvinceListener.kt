package nl.rubensten.texifyidea.run.evince

import com.intellij.execution.process.ProcessEvent
import com.intellij.execution.process.ProcessListener
import com.intellij.openapi.util.Key
import nl.rubensten.texifyidea.TeXception
import nl.rubensten.texifyidea.run.LatexRunConfiguration

/**
 * Opens Evince after the compile process terminated (correctly).
 *
 * @author Thomas Schouten
 */
class OpenEvinceListener(val runConfig: LatexRunConfiguration) : ProcessListener {

    override fun processTerminated(event: ProcessEvent) {
        if (event.exitCode == 0 && isEvinceAvailable()) {
            try {
                EvinceConversation.openFile(runConfig.outputFilePath)
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