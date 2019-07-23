package nl.hannahsten.texifyidea.run.makeindex

import com.intellij.execution.impl.RunConfigurationBeforeRunProvider
import com.intellij.execution.impl.RunManagerImpl
import com.intellij.execution.process.ProcessEvent
import com.intellij.execution.process.ProcessListener
import com.intellij.execution.runners.ExecutionEnvironment
import com.intellij.openapi.util.Key
import nl.hannahsten.texifyidea.run.latex.LatexConfigurationFactory
import nl.hannahsten.texifyidea.run.latex.LatexRunConfiguration

/**
 * Run makeindex and then latex again (twice).
 */
class RunMakeindexListener(
        private val latexRunConfig: LatexRunConfiguration,
        private val environment: ExecutionEnvironment
) : ProcessListener {

    override fun processTerminated(event: ProcessEvent) {
        val runManager = RunManagerImpl.getInstanceImpl(environment.project)

        val makeindexSettings = runManager.createConfiguration(
                "",
                LatexConfigurationFactory(MakeindexRunConfigurationType())
        )

        runManager.addConfiguration(makeindexSettings)

        val makeindexRunConfiguration = makeindexSettings.configuration as MakeindexRunConfiguration

        makeindexRunConfiguration.latexRunConfiguration = latexRunConfig

        // Run makeindex
        RunConfigurationBeforeRunProvider.doExecuteTask(environment, makeindexSettings, null)

        // todo copy .ind file

        // todo run latex twice
    }

    override fun onTextAvailable(p0: ProcessEvent, p1: Key<*>) {}

    override fun processWillTerminate(p0: ProcessEvent, p1: Boolean) {}

    override fun startNotified(p0: ProcessEvent) {}
}