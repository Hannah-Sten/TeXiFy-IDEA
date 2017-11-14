package nl.rubensten.texifyidea.run

import com.intellij.execution.process.ProcessEvent
import com.intellij.execution.process.ProcessListener
import com.intellij.openapi.util.Key
import com.intellij.openapi.util.SystemInfo

/**
 * @author Sten Wessel
 */
class OpenSumatraListener(val runConfig: LatexRunConfiguration) : ProcessListener {

    override fun processTerminated(event: ProcessEvent?) {
        if (event?.exitCode == 0 && SystemInfo.isWindows) {
            SumatraConversation.openFile(runConfig.outputFilePath, start = true)
        }
    }

    override fun onTextAvailable(p0: ProcessEvent?, p1: Key<*>?) {
        // Do nothing.
    }

    override fun processWillTerminate(p0: ProcessEvent?, p1: Boolean) {
        // Do nothing.
    }

    override fun startNotified(p0: ProcessEvent?) {
        // Do nothing.
    }
}
