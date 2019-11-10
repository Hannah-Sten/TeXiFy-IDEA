package nl.hannahsten.texifyidea.run.makeindex

import com.intellij.execution.impl.RunConfigurationBeforeRunProvider
import com.intellij.execution.impl.RunManagerImpl
import com.intellij.execution.process.ProcessEvent
import com.intellij.execution.process.ProcessListener
import com.intellij.execution.runners.ExecutionEnvironment
import com.intellij.openapi.util.Key
import com.intellij.openapi.util.io.FileUtil
import nl.hannahsten.texifyidea.run.latex.LatexConfigurationFactory
import nl.hannahsten.texifyidea.run.latex.LatexRunConfiguration
import nl.hannahsten.texifyidea.util.appendExtension
import java.io.File

/**
 * Run makeindex and then latex again (twice).
 */
class RunMakeindexListener(
        private val runConfig: LatexRunConfiguration,
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

        makeindexRunConfiguration.latexRunConfiguration = runConfig
        makeindexRunConfiguration.setSuggestedName()

        // Run makeindex
        RunConfigurationBeforeRunProvider.doExecuteTask(environment, makeindexSettings, null)

        try {
            if (!copyIndexFile()) return

            // Don't schedule more latex runs if bibtex is used, because that will already schedule the extra runs
            if (runConfig.bibRunConfig == null) {
                // LaTeX twice
                runConfig.isFirstRunConfig = false
                val latexSettings = RunManagerImpl.getInstanceImpl(environment.project).getSettings(runConfig)
                        ?: return
                runConfig.isLastRunConfig = false
                RunConfigurationBeforeRunProvider.doExecuteTask(environment, latexSettings, null)
                runConfig.isLastRunConfig = true
                RunConfigurationBeforeRunProvider.doExecuteTask(environment, latexSettings, null)
            }
        } finally {
            runConfig.isLastRunConfig = false
            runConfig.isFirstRunConfig = true
        }
    }

    /**
     * Copy .ind file where it was generated to next to the main file, so an index package will hopefully find it.
     */
    private fun copyIndexFile(): Boolean {
        val mainFile = runConfig.mainFile ?: return false
        val workDir = runConfig.getAuxilDirectory() ?: return false

        val indexFileSource = workDir.path + '/' + mainFile.nameWithoutExtension + ".ind"
        val indexFileDestination = mainFile.path.dropLast(4).appendExtension("ind")
        FileUtil.copy(File(indexFileSource), File(indexFileDestination))
        return true
    }

    override fun onTextAvailable(p0: ProcessEvent, p1: Key<*>) {}

    override fun processWillTerminate(p0: ProcessEvent, p1: Boolean) {}

    override fun startNotified(p0: ProcessEvent) {}
}