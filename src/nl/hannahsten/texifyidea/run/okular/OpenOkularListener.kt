package nl.hannahsten.texifyidea.run.okular

import com.intellij.execution.process.ProcessEvent
import com.intellij.execution.process.ProcessListener
import com.intellij.openapi.util.Key
import nl.hannahsten.texifyidea.TeXception
import nl.hannahsten.texifyidea.run.LatexRunConfiguration
import nl.hannahsten.texifyidea.run.runCommand
import org.jetbrains.concurrency.runAsync

/**
 * Execute a forward search with Okular after the compilation is done.
 */
class OpenOkularListener(val runConfig: LatexRunConfiguration, val texFilePath: String, val line: Int) : ProcessListener{
    override fun processTerminated(event: ProcessEvent) {
        if (event.exitCode == 0) {
            runAsync {
                try {
                    OkularConversation.forwardSearch(runConfig.outputFilePath, texFilePath, line)
                }
                catch (ignored: TeXception) {
                }
            }
        }
    }

    override fun onTextAvailable(event: ProcessEvent, outputType: Key<*>) {
        // Do nothing.
    }

    override fun processWillTerminate(event: ProcessEvent, willBeDestroyed: Boolean) {
        // Do nothing.
    }

    override fun startNotified(event: ProcessEvent) {
        // Do nothing.
    }
}