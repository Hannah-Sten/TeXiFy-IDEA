package nl.hannahsten.texifyidea.editor.autocompile

import com.intellij.execution.process.ProcessEvent
import com.intellij.execution.process.ProcessListener
import com.intellij.openapi.util.Key
import nl.hannahsten.texifyidea.run.latex.LatexRunConfiguration

/**
 * When autocompile is enabled, update autocompile state when compilation is done.
 */
class AutoCompileDoneListener(
    private val runConfig: LatexRunConfiguration,
) : ProcessListener {

    override fun processTerminated(event: ProcessEvent) {
        runConfig.isAutoCompiling = false
        AutoCompileState.compilationFinished()
    }

    override fun onTextAvailable(event: ProcessEvent, outputType: Key<*>) {
    }

    override fun processWillTerminate(event: ProcessEvent, willBeDestroyed: Boolean) {
    }

    override fun startNotified(event: ProcessEvent) {
    }
}
