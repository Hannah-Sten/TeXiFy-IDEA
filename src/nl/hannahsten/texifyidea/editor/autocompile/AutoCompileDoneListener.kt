package nl.hannahsten.texifyidea.editor.autocompile

import com.intellij.execution.process.ProcessEvent
import com.intellij.execution.process.ProcessListener
import com.intellij.openapi.util.Key

/**
 * When autocompile is enabled, update autocompile state when compilation is done.
 */
class AutoCompileDoneListener : ProcessListener {

    override fun processTerminated(event: ProcessEvent) {
        AutoCompileState.compilationFinished()
    }

    override fun onTextAvailable(event: ProcessEvent, outputType: Key<*>) {
    }

    override fun processWillTerminate(event: ProcessEvent, willBeDestroyed: Boolean) {
    }

    override fun startNotified(event: ProcessEvent) {
    }
}