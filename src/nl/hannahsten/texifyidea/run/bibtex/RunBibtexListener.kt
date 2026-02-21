package nl.hannahsten.texifyidea.run.bibtex

import com.intellij.execution.RunnerAndConfigurationSettings
import com.intellij.execution.impl.RunConfigurationBeforeRunProvider
import com.intellij.execution.process.ProcessEvent
import com.intellij.execution.process.ProcessListener
import com.intellij.execution.runners.ExecutionEnvironment
import com.intellij.openapi.util.Key
import nl.hannahsten.texifyidea.run.latex.LatexRunExecutionState
import nl.hannahsten.texifyidea.run.latex.LatexRunConfiguration
import nl.hannahsten.texifyidea.run.latex.LatexRerunScheduler

/**
 * @author Sten Wessel
 */
class RunBibtexListener(
    private val bibtexSettings: RunnerAndConfigurationSettings,
    private val latexConfiguration: LatexRunConfiguration,
    private val environment: ExecutionEnvironment,
    private val executionState: LatexRunExecutionState,
    private val runLatexAfterwards: Boolean = true
) : ProcessListener {

    override fun processTerminated(event: ProcessEvent) {
        if (event.exitCode != 0) {
            return
        }

        // Run bibtex compiler (blocking execution)
        if (!RunConfigurationBeforeRunProvider.doExecuteTask(environment, bibtexSettings, null)) {
            return
        }

        if (runLatexAfterwards) {
            try {
                LatexRerunScheduler.runLatexTwice(environment, latexConfiguration, executionState)
            }
            finally {
                executionState.resetAfterAuxChain()
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
