package nl.hannahsten.texifyidea.run.latex

import com.intellij.execution.impl.RunConfigurationBeforeRunProvider
import com.intellij.execution.impl.RunManagerImpl
import com.intellij.execution.runners.ExecutionEnvironment
import com.intellij.execution.RunnerAndConfigurationSettings

internal object LatexRerunScheduler {

    fun runLatexTwice(
        environment: ExecutionEnvironment,
        latexRunConfig: LatexRunConfiguration,
        executionState: LatexRunExecutionState,
        suppressBeforeRunTasks: Boolean = false,
    ) {
        val latexSettings = RunManagerImpl.getInstanceImpl(environment.project).getSettings(latexRunConfig)
            ?: return

        executionState.beginAuxChain()

        withOptionalBeforeRunTasksSuppressed(latexSettings, suppressBeforeRunTasks) {
            executionState.markIntermediatePass()
            RunConfigurationBeforeRunProvider.doExecuteTask(environment, latexSettings, null)

            executionState.markLastPass()
            RunConfigurationBeforeRunProvider.doExecuteTask(environment, latexSettings, null)
        }
    }

    private inline fun withOptionalBeforeRunTasksSuppressed(
        latexSettings: RunnerAndConfigurationSettings,
        suppress: Boolean,
        block: () -> Unit,
    ) {
        if (!suppress) {
            block()
            return
        }

        val configuration = latexSettings.configuration
        val beforeRunTasks = configuration.beforeRunTasks

        try {
            configuration.beforeRunTasks = mutableListOf()
            block()
        }
        finally {
            configuration.beforeRunTasks = beforeRunTasks
        }
    }
}
