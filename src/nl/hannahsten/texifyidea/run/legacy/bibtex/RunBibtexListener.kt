package nl.hannahsten.texifyidea.run.legacy.bibtex

import com.intellij.execution.RunnerAndConfigurationSettings
import com.intellij.execution.impl.RunConfigurationBeforeRunProvider
import com.intellij.execution.impl.RunManagerImpl
import com.intellij.execution.process.ProcessEvent
import com.intellij.execution.process.ProcessListener
import com.intellij.execution.runners.ExecutionEnvironment
import com.intellij.openapi.util.Key
import nl.hannahsten.texifyidea.run.LatexRunConfiguration

/**
 * @author Sten Wessel
 */
class RunBibtexListener(
    private val bibtexSettings: RunnerAndConfigurationSettings,
    private val latexConfiguration: LatexRunConfiguration,
    private val environment: ExecutionEnvironment,
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
            // Mark the next latex runs to exclude bibtex compilation
            latexConfiguration.isFirstRunConfig = false
            try {
                val latexSettings = RunManagerImpl.getInstanceImpl(environment.project).getSettings(latexConfiguration)
                    ?: return

                // Compile twice to fix references etc
                // Mark the next latex run as not being the final one, to avoid for instance opening the pdf viewer too early (with possible multiple open pdfs as a result, or a second open would fail because of a write lock)
                latexConfiguration.isLastRunConfig = false
                RunConfigurationBeforeRunProvider.doExecuteTask(environment, latexSettings, null)
                latexConfiguration.isLastRunConfig = true
                RunConfigurationBeforeRunProvider.doExecuteTask(environment, latexSettings, null)
            }
            finally {
                latexConfiguration.isLastRunConfig = false
                latexConfiguration.isFirstRunConfig = true
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
