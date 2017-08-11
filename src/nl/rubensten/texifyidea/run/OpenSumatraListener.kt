package nl.rubensten.texifyidea.run

import com.intellij.execution.process.ProcessEvent
import com.intellij.execution.process.ProcessListener
import com.intellij.openapi.util.Key
import com.intellij.openapi.util.SystemInfo

/**
 *
 * @author Sten Wessel
 */
class OpenSumatraListener(val runConfig: LatexRunConfiguration) : ProcessListener {
    override fun processTerminated(e: ProcessEvent?) {
        if (e?.exitCode == 0 && SystemInfo.isWindows) {
            SumatraConversation.openFile(runConfig.outputFilePath, focus = true)
        }
    }

    override fun onTextAvailable(p0: ProcessEvent?, p1: Key<*>?) {
    }

    override fun processWillTerminate(p0: ProcessEvent?, p1: Boolean) {
    }

    override fun startNotified(p0: ProcessEvent?) {
    }
}
