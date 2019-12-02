package nl.hannahsten.texifyidea.run.makeindex

import com.intellij.execution.RunnerAndConfigurationSettings
import com.intellij.execution.impl.RunConfigurationBeforeRunProvider
import com.intellij.execution.impl.RunManagerImpl
import com.intellij.execution.process.ProcessEvent
import com.intellij.execution.process.ProcessListener
import com.intellij.execution.runners.ExecutionEnvironment
import com.intellij.notification.Notification
import com.intellij.notification.NotificationType
import com.intellij.notification.Notifications
import com.intellij.openapi.util.Key
import com.intellij.openapi.util.io.FileUtil
import nl.hannahsten.texifyidea.run.latex.LatexConfigurationFactory
import nl.hannahsten.texifyidea.run.latex.LatexRunConfiguration
import nl.hannahsten.texifyidea.util.appendExtension
import java.io.File
import java.io.FileNotFoundException

/**
 * Run makeindex and then latex again (twice).
 */
class RunMakeindexListener(
        private val latexRunConfig: LatexRunConfiguration,
        private val environment: ExecutionEnvironment
) : ProcessListener {

    override fun processTerminated(event: ProcessEvent) {

        // Only create new one if there is none yet
        val runConfigSettings: RunnerAndConfigurationSettings =
        if (latexRunConfig.makeindexRunConfig == null) {
            generateIndexConfig()
        }
        else {
            latexRunConfig.makeindexRunConfig ?: return
        }

        // Run makeindex
        RunConfigurationBeforeRunProvider.doExecuteTask(environment, runConfigSettings, null)

        try {
            val makeindexRunConfig = runConfigSettings.configuration as MakeindexRunConfiguration
            val options = makeindexRunConfig.getMakeindexOptions()
            val indexFilename = options.getOrDefault("name", makeindexRunConfig.mainFile?.nameWithoutExtension)?.appendExtension("ind") ?: return

            if (!copyIndexFile(indexFilename)) return

            // Don't schedule more latex runs if bibtex is used, because that will already schedule the extra runs
            if (latexRunConfig.bibRunConfig == null) {
                // LaTeX twice
                latexRunConfig.isFirstRunConfig = false
                val latexSettings = RunManagerImpl.getInstanceImpl(environment.project).getSettings(latexRunConfig)
                        ?: return
                latexRunConfig.isLastRunConfig = false
                RunConfigurationBeforeRunProvider.doExecuteTask(environment, latexSettings, null)
                latexRunConfig.isLastRunConfig = true
                RunConfigurationBeforeRunProvider.doExecuteTask(environment, latexSettings, null)
            }
        } finally {
            latexRunConfig.isLastRunConfig = false
            latexRunConfig.isFirstRunConfig = true
        }
    }

    private fun generateIndexConfig(): RunnerAndConfigurationSettings {
        val runManager = RunManagerImpl.getInstanceImpl(environment.project)

        val makeindexRunConfigSettings = runManager.createConfiguration(
                "",
                LatexConfigurationFactory(MakeindexRunConfigurationType())
        )

        val makeindexRunConfiguration = makeindexRunConfigSettings.configuration as MakeindexRunConfiguration

        makeindexRunConfiguration.mainFile = latexRunConfig.mainFile
        makeindexRunConfiguration.workingDirectory = latexRunConfig.getAuxilDirectory()
        makeindexRunConfiguration.setSuggestedName()

        runManager.addConfiguration(makeindexRunConfigSettings)

        latexRunConfig.makeindexRunConfig = makeindexRunConfigSettings
        return makeindexRunConfigSettings
    }

    /**
     * Copy .ind file where it was generated to next to the main file, so an index package will hopefully find it.
     *
     * @param indexFileName Filename of the .ind file, with extension.
     */
    private fun copyIndexFile(indexFileName: String): Boolean {
        val mainFile = latexRunConfig.mainFile ?: return false
        val workDir = latexRunConfig.getAuxilDirectory() ?: return false

        val indexFileSource = workDir.path + '/' + indexFileName
        val indexFileDestination = File(mainFile.path).parent + '/' + indexFileName
        try {
            FileUtil.copy(File(indexFileSource), File(indexFileDestination))
        } catch (e: FileNotFoundException) {
            Notifications.Bus.notify(Notification("nl.hannahsten.texifyidea", "Error when copying the index file", e.message ?: "", NotificationType.ERROR), environment.project)
        }
        return true
    }

    override fun onTextAvailable(p0: ProcessEvent, p1: Key<*>) {}

    override fun processWillTerminate(p0: ProcessEvent, p1: Boolean) {}

    override fun startNotified(p0: ProcessEvent) {}
}