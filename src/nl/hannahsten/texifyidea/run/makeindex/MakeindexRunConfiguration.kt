package nl.hannahsten.texifyidea.run.makeindex

import com.intellij.execution.ExecutionException
import com.intellij.execution.Executor
import com.intellij.execution.configurations.*
import com.intellij.execution.runners.ExecutionEnvironment
import com.intellij.openapi.application.runReadAction
import com.intellij.openapi.options.SettingsEditor
import com.intellij.openapi.project.Project
import nl.hannahsten.texifyidea.index.LatexCommandsIndex
import nl.hannahsten.texifyidea.run.latex.LatexRunConfiguration
import nl.hannahsten.texifyidea.util.PackageUtils
import nl.hannahsten.texifyidea.util.files.psiFile

/**
 * Run configuration for running the makeindex tool.
 */
class MakeindexRunConfiguration(
        project: Project,
        factory: ConfigurationFactory,
        name: String
        ) : RunConfigurationBase<MakeindexCommandLineState>(project, factory, name), LocatableConfiguration {

    var latexRunConfiguration: LatexRunConfiguration = LatexRunConfiguration(project, factory, "LaTeX")

    override fun getState(executor: Executor, environment: ExecutionEnvironment): RunProfileState {
        return MakeindexCommandLineState(environment, latexRunConfiguration, getIndexPackageOptions(), getMakeindexOptions())
    }

    override fun getConfigurationEditor(): SettingsEditor<out RunConfiguration> = throw NotImplementedError()

    override fun isGeneratedName() = name == suggestedName()

    override fun suggestedName() = "index"

    fun setSuggestedName() {
        name = suggestedName()
    }

    /**
     * Get package options for included index packages.
     */
    fun getIndexPackageOptions(): List<String> {
        return runReadAction {
            // Find index package options
            val mainPsiFile = latexRunConfiguration.mainFile?.psiFile(project) ?: throw ExecutionException("Main psifile not found")
            LatexCommandsIndex.getItemsInFileSet(mainPsiFile)
                    .filter { it.commandToken.text in PackageUtils.PACKAGE_COMMANDS }
                    .filter { command -> command.requiredParameters.any { it == "imakeidx" } }
                    .flatMap { it.optionalParameters }
        }
    }

    /**
     * Get optional parameters of the \makeindex command. If an option key does not have a value it will map to the empty string.
     */
    fun getMakeindexOptions(): HashMap<String, String> {
        return runReadAction {
            val mainPsiFile = latexRunConfiguration.mainFile?.psiFile(project) ?: throw ExecutionException("Main psifile not found")
            val makeindexOptions = HashMap<String, String>()
            LatexCommandsIndex.getItemsInFileSet(mainPsiFile)
                    .filter { it.commandToken.text == "\\makeindex" }
                    .flatMap { it.optionalParameters }
                    .map { it.split("=") }
                    .forEach {
                        if (it.size == 1) {
                            makeindexOptions[it.first()] = ""
                        } else if (it.size == 2) {
                            makeindexOptions[it.first()] = it.last()
                        }
                    }
            makeindexOptions
        }
    }
}