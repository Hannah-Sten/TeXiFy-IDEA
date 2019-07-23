package nl.hannahsten.texifyidea.run.makeindex

import com.intellij.execution.Executor
import com.intellij.execution.configurations.*
import com.intellij.execution.runners.ExecutionEnvironment
import com.intellij.openapi.options.SettingsEditor
import com.intellij.openapi.project.Project
import nl.hannahsten.texifyidea.run.latex.LatexRunConfiguration

class MakeindexRunConfiguration(
        project: Project,
        factory: ConfigurationFactory,
        name: String
        ) : RunConfigurationBase<MakeindexCommandLineState>(project, factory, name), LocatableConfiguration {

    var latexRunConfiguration: LatexRunConfiguration = LatexRunConfiguration(project, factory, "LaTeX")

    override fun getState(executor: Executor, environment: ExecutionEnvironment): RunProfileState {
        return MakeindexCommandLineState(environment, latexRunConfiguration)
    }

    override fun getConfigurationEditor(): SettingsEditor<out RunConfiguration> = throw NotImplementedError()

    override fun isGeneratedName(): Boolean = throw NotImplementedError()
}