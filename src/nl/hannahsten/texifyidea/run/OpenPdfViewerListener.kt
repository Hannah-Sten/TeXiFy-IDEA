package nl.hannahsten.texifyidea.run

import com.intellij.execution.process.ProcessEvent
import com.intellij.execution.process.ProcessListener
import com.intellij.openapi.util.Key
import nl.hannahsten.texifyidea.TeXception
import java.io.IOException

/**
 * Run the given command after the process has succeeded.
 */
class OpenPdfViewerListener(val command: Array<String>, val failSilently: Boolean = false) : ProcessListener {
    override fun processTerminated(event: ProcessEvent) {
        if (event.exitCode == 0) {
            try {
                ProcessBuilder(*command).start()
            }
            catch (e: IOException) {
                if (!failSilently) {
                    throw TeXception("An error occured when trying to open the pdf using $command with message ${e.message}")
                }
            }
        }
    }

    override fun processWillTerminate(event: ProcessEvent, willBeDestroyed: Boolean) {
    }

    override fun startNotified(event: ProcessEvent) {
    }

    override fun onTextAvailable(event: ProcessEvent, outputType: Key<*>) {
    }
}