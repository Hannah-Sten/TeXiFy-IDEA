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
import nl.hannahsten.texifyidea.util.Magic
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
            val baseFileName = options.getOrDefault("name", makeindexRunConfig.mainFile?.nameWithoutExtension) ?: return

            // todo cleanup files, but only if they didn't exist already (copied manually by user)
            val copiedFiles = copyIndexFiles(baseFileName)


            // Don't schedule more latex runs if bibtex is used, because that will already schedule the extra runs
            if (latexRunConfig.bibRunConfigs.isEmpty()) {
                // LaTeX twice
                latexRunConfig.isFirstRunConfig = false
                val latexSettings = RunManagerImpl.getInstanceImpl(environment.project).getSettings(latexRunConfig)
                        ?: return
                latexRunConfig.isLastRunConfig = false
                RunConfigurationBeforeRunProvider.doExecuteTask(environment, latexSettings, null)
                latexRunConfig.isLastRunConfig = true
                RunConfigurationBeforeRunProvider.doExecuteTask(environment, latexSettings, null)
            }
        }
        finally {
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
     * Copy .ind and similar files from where they were generated to next to the main file, so an index package will hopefully find it.
     *
     * @param baseFileName Filename of generated files without extension.
     *
     * @return List of files that were copied.
     */
    private fun copyIndexFiles(baseFileName: String): List<File> {

        val mainFile = latexRunConfig.mainFile ?: return emptyList()
        val workDir = latexRunConfig.getAuxilDirectory() ?: return emptyList()

        val copiedFiles = mutableListOf<File>()

        // Just try all extensions for files that need to be copied
        for (extension in Magic.File.indexFileExtensions) {

            val indexFileName = baseFileName.appendExtension(extension)
            // todo only copy file if exists
            val indexFileSource = File(workDir.path + '/' + indexFileName)
            if (!indexFileSource.isFile) continue
            val indexFileDestination = File(File(mainFile.path).parent + '/' + indexFileName)

            try {
                FileUtil.copy(indexFileSource, indexFileDestination)
                copiedFiles.add(indexFileDestination)
            }
            catch (ignored: FileNotFoundException) {}
        }
        return copiedFiles
    }

    override fun onTextAvailable(p0: ProcessEvent, p1: Key<*>) {}

    override fun processWillTerminate(p0: ProcessEvent, p1: Boolean) {}

    override fun startNotified(p0: ProcessEvent) {}
}