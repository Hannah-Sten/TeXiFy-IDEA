package nl.hannahsten.texifyidea.run

import com.intellij.execution.RunnerAndConfigurationSettings
import com.intellij.execution.impl.RunConfigurationBeforeRunProvider
import com.intellij.execution.impl.RunManagerImpl
import com.intellij.execution.process.ProcessEvent
import com.intellij.execution.process.ProcessListener
import com.intellij.execution.runners.ExecutionEnvironment
import com.intellij.openapi.util.Key

/**
 * @author Sten Wessel
 */
class RunBibtexListener(
        private val bibtexSettings: RunnerAndConfigurationSettings,
        private val latexConfiguration: LatexRunConfiguration,
        private val environment: ExecutionEnvironment
) : ProcessListener {

    override fun processTerminated(event: ProcessEvent) {
        if (event.exitCode != 0) {
            return
        }

        // Run bibtex compiler (blocking execution)
        RunConfigurationBeforeRunProvider.doExecuteTask(environment, bibtexSettings, null)

        // Mark the next latex runs to exclude bibtex compilation
        latexConfiguration.isSkipBibtex = true
        try {
            val latexSettings = RunManagerImpl.getInstanceImpl(environment.project).getSettings(latexConfiguration)
                    ?: return

            // Compile twice to fix references etc
            // Mark the next latex run as not being the final one, to avoid for instance opening the pdf viewer too early (with possible multiple open pdfs as a result, or a second open would fail because of a write lock)
            latexConfiguration.isLastRunConfig = false
            RunConfigurationBeforeRunProvider.doExecuteTask(environment, latexSettings, null)
            latexConfiguration.isLastRunConfig = true
            RunConfigurationBeforeRunProvider.doExecuteTask(environment, latexSettings, null)
            latexConfiguration.isLastRunConfig = false
        }
        finally {
            latexConfiguration.isSkipBibtex = false
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
