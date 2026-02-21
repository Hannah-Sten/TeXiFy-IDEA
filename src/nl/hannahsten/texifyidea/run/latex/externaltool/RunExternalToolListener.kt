package nl.hannahsten.texifyidea.run.latex.externaltool

import com.intellij.execution.RunnerAndConfigurationSettings
import com.intellij.execution.impl.RunConfigurationBeforeRunProvider
import com.intellij.execution.impl.RunManagerImpl
import com.intellij.execution.process.ProcessEvent
import com.intellij.execution.process.ProcessListener
import com.intellij.execution.runners.ExecutionEnvironment
import com.intellij.openapi.application.ReadAction
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Key
import com.intellij.openapi.vfs.VirtualFile
import nl.hannahsten.texifyidea.lang.LatexLib
import nl.hannahsten.texifyidea.run.compiler.ExternalTool
import nl.hannahsten.texifyidea.run.latex.LatexConfigurationFactory
import nl.hannahsten.texifyidea.run.latex.LatexRerunScheduler
import nl.hannahsten.texifyidea.run.latex.LatexRunExecutionState
import nl.hannahsten.texifyidea.run.latex.LatexRunConfiguration
import nl.hannahsten.texifyidea.util.files.psiFile
import nl.hannahsten.texifyidea.util.includedPackagesInFileset

/**
 * Run the external tool.
 */
class RunExternalToolListener(
    private val latexRunConfig: LatexRunConfiguration,
    private val environment: ExecutionEnvironment,
    private val executionState: LatexRunExecutionState,
) : ProcessListener {

    companion object {

        /**
         * Check the contents of the LaTeX fileset to find out if any external tools are needed.
         */
        fun getRequiredExternalTools(mainFile: VirtualFile?, project: Project): Set<ExternalTool> {
            val usedPackages = ReadAction.compute<Set<LatexLib>, RuntimeException> {
                mainFile?.psiFile(project)?.includedPackagesInFileset() ?: emptySet()
            }

            val externalTools = mutableSetOf<ExternalTool>()

            if (LatexLib.PYTHONTEX in usedPackages) {
                externalTools.add(ExternalTool.PYTHONTEX)
            }

            return externalTools
        }
    }

    override fun processTerminated(event: ProcessEvent) {
        try {
            // Only create new one if there is none yet
            val runConfigSettingsList =
                latexRunConfig.externalToolRunConfigs.ifEmpty {
                    generateExternalToolConfigs()
                }

            // Run all run configurations
            for (runConfigSettings in runConfigSettingsList) {
                RunConfigurationBeforeRunProvider.doExecuteTask(environment, runConfigSettings, null)
            }

            scheduleLatexRuns()
        }
        finally {
            executionState.resetAfterAuxChain()
        }
    }

    private fun scheduleLatexRuns() {
        // Don't schedule more latex runs if bibtex is used, because that will already schedule the extra runs
        if (latexRunConfig.bibRunConfigs.isEmpty() && latexRunConfig.makeindexRunConfigs.isEmpty()) {
            LatexRerunScheduler.runLatexTwice(environment, latexRunConfig, executionState, suppressBeforeRunTasks = true)
        }
    }

    /**
     * Generate the extra run configs if needed.
     */
    private fun generateExternalToolConfigs(): Set<RunnerAndConfigurationSettings> {
        val runManager = RunManagerImpl.getInstanceImpl(environment.project)
        val mainFile = executionState.resolvedMainFile
        val tools = getRequiredExternalTools(mainFile, environment.project)

        val runConfigs = mutableSetOf<RunnerAndConfigurationSettings>()

        for (tool in tools) {
            val runConfigSettings = runManager.createConfiguration(
                "",
                LatexConfigurationFactory(ExternalToolRunConfigurationType())
            )

            val runConfig = runConfigSettings.configuration as ExternalToolRunConfiguration

            runConfig.mainFile = mainFile
            runConfig.program = tool
            runConfig.setSuggestedName()
            runConfig.workingDirectory = executionState.resolvedAuxDir

            runManager.addConfiguration(runConfigSettings)
            runConfigs.add(runConfigSettings)
        }

        latexRunConfig.externalToolRunConfigs = runConfigs
        return runConfigs
    }

    override fun onTextAvailable(p0: ProcessEvent, p1: Key<*>) {}

    override fun processWillTerminate(p0: ProcessEvent, p1: Boolean) {}

    override fun startNotified(p0: ProcessEvent) {}
}
