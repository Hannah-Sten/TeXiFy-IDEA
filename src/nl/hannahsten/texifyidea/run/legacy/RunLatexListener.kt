package nl.hannahsten.texifyidea.run.legacy

import com.intellij.execution.impl.RunConfigurationBeforeRunProvider
import com.intellij.execution.impl.RunManagerImpl
import com.intellij.execution.process.ProcessEvent
import com.intellij.execution.process.ProcessListener
import com.intellij.execution.runners.ExecutionEnvironment
import com.intellij.openapi.util.Key
import nl.hannahsten.texifyidea.run.LatexRunConfiguration

/**
 * Compile LaTeX (again) once a process is terminated and initiate forward search.
 *
 * @author Thomas Schouten
 */
class RunLatexListener(
    private val runConfig: LatexRunConfiguration,
    private val environment: ExecutionEnvironment
) : ProcessListener {

    override fun processTerminated(event: ProcessEvent) {
        if (event.exitCode != 0) {
            return
        }

        val latexSettings = RunManagerImpl.getInstanceImpl(environment.project).getSettings(runConfig)
            ?: return
        runConfig.isLastRunConfig = true
        RunConfigurationBeforeRunProvider.doExecuteTask(environment, latexSettings, null)
        runConfig.isLastRunConfig = false
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
