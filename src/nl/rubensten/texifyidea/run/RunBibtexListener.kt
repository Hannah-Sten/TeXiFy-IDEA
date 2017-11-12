package nl.rubensten.texifyidea.run

import com.intellij.execution.RunnerAndConfigurationSettings
import com.intellij.execution.impl.RunConfigurationBeforeRunProvider
import com.intellij.execution.impl.RunManagerImpl
import com.intellij.execution.process.ProcessEvent
import com.intellij.execution.process.ProcessListener
import com.intellij.execution.runners.ExecutionEnvironment
import com.intellij.openapi.util.Key

/**
 *
 * @author Sten Wessel
 */
class RunBibtexListener(
    private val bibtexSettings: RunnerAndConfigurationSettings,
    private val latexConfiguration: LatexRunConfiguration,
    private val environment: ExecutionEnvironment
) : ProcessListener {

    override fun processTerminated(e: ProcessEvent?) {
        if (e?.exitCode != 0) {
            return
        }

        // Run bibtex compiler (blocking execution)
        RunConfigurationBeforeRunProvider.doExecuteTask(environment, bibtexSettings)

        // Mark the next latex runs to exclude bibtex compilation
        latexConfiguration.isSkipBibtex = true
        try {
            val latexSettings = RunManagerImpl.getInstanceImpl(environment.project).getSettings(latexConfiguration) ?: return

            repeat(2) {
                RunConfigurationBeforeRunProvider.doExecuteTask(environment, latexSettings)
            }
        }
        finally {
            latexConfiguration.isSkipBibtex = false
        }
    }

    override fun onTextAvailable(p0: ProcessEvent?, p1: Key<*>?) {
    }

    override fun processWillTerminate(p0: ProcessEvent?, p1: Boolean) {
    }

    override fun startNotified(p0: ProcessEvent?) {
    }
}
