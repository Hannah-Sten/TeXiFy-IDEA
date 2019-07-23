package nl.hannahsten.texifyidea.run.makeindex

import com.intellij.execution.Executor
import com.intellij.execution.RunnerAndConfigurationSettings
import com.intellij.execution.configurations.*
import com.intellij.execution.impl.RunManagerImpl
import com.intellij.execution.runners.ExecutionEnvironment
import com.intellij.openapi.options.SettingsEditor
import com.intellij.openapi.project.Project
import nl.hannahsten.texifyidea.run.latex.LatexRunConfiguration

class MakeindexRunConfiguration(
        project: Project,
        factory: ConfigurationFactory,
        name: String
        ) : RunConfigurationBase<MakeindexCommandLineState>(project, factory, name), LocatableConfiguration {

    // Keep a reference to the latex run config, to know what to run makeindex on and where
    private var latexRunConfigId = ""

    // todo set somewhere
    var latexRunConfig: RunnerAndConfigurationSettings?
        get() = RunManagerImpl.getInstanceImpl(project).getConfigurationById(latexRunConfigId)
        set(latexRunConfig) {
            this.latexRunConfigId = latexRunConfig?.uniqueID ?: ""
        }

    override fun getState(executor: Executor, environment: ExecutionEnvironment): RunProfileState {
        return MakeindexCommandLineState(environment, this)
    }

    override fun getConfigurationEditor(): SettingsEditor<out RunConfiguration> = throw NotImplementedError()

    override fun isGeneratedName(): Boolean = throw NotImplementedError()
}