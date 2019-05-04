package nl.rubensten.texifyidea.run

import com.intellij.execution.process.ProcessEvent
import com.intellij.execution.process.ProcessListener
import com.intellij.openapi.util.Key

/**
 * Run the given command after the process has succeeded.
 */
class CommandProcessListener(val command: String) : ProcessListener {
    override fun processTerminated(event: ProcessEvent) {
        if (event.exitCode == 0) {
            Runtime.getRuntime().exec(command)
        }
    }

    override fun processWillTerminate(event: ProcessEvent, willBeDestroyed: Boolean) {
    }

    override fun startNotified(event: ProcessEvent) {
    }

    override fun onTextAvailable(event: ProcessEvent, outputType: Key<*>) {
    }

}