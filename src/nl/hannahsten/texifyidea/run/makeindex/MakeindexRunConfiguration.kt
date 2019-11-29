package nl.hannahsten.texifyidea.run.makeindex

import com.intellij.execution.ExecutionException
import com.intellij.execution.Executor
import com.intellij.execution.configurations.*
import com.intellij.execution.runners.ExecutionEnvironment
import com.intellij.openapi.application.runReadAction
import com.intellij.openapi.options.SettingsEditor
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import nl.hannahsten.texifyidea.index.LatexCommandsIndex
import nl.hannahsten.texifyidea.run.compiler.MakeindexProgram
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

    // The following variables are saved in the MakeindexSettingsEditor
    var makeindexProgram: MakeindexProgram? = null
    var mainFile: VirtualFile? = null
    var workingDirectory: VirtualFile? = null

    override fun getState(executor: Executor, environment: ExecutionEnvironment): RunProfileState {
        val makeindexOptions = getMakeindexOptions()
        val indexProgram = makeindexProgram ?: findMakeindexProgram(getIndexPackageOptions(), makeindexOptions)

        return MakeindexCommandLineState(environment, mainFile, workingDirectory, makeindexOptions, indexProgram)
    }

    override fun getConfigurationEditor(): SettingsEditor<out RunConfiguration> = MakeindexSettingsEditor(project)

    override fun isGeneratedName() = name == suggestedName()

    override fun suggestedName() = "index"

    fun setSuggestedName() {
        name = suggestedName()
    }

    /**
     * Try to find out which index program the user wants to use, based on the given options.
     * Will also set [makeindexProgram] if not already set.
     */
    fun findMakeindexProgram(indexPackageOptions: List<String>, makeindexOptions: HashMap<String, String>): MakeindexProgram {

        var indexProgram = if (indexPackageOptions.contains("xindy")) MakeindexProgram.XINDY else MakeindexProgram.MAKEINDEX

        // Possible extra settings to override the indexProgram, see the imakeidx docs
        if (makeindexOptions.contains("makeindex")) {
            indexProgram = MakeindexProgram.MAKEINDEX
        }
        else if (makeindexOptions.contains("xindy") || makeindexOptions.contains("texindy")) {
            indexProgram = MakeindexProgram.XINDY
        }
        else if (makeindexOptions.contains("truexindy")) {
            indexProgram = MakeindexProgram.TRUEXINDY
        }

        // todo this will override user chosen setting?
        if (makeindexProgram == null) {
            makeindexProgram = indexProgram
        }
        return indexProgram
    }

    /**
     * Get package options for included index packages.
     */
    fun getIndexPackageOptions(): List<String> {
        return runReadAction {
            // Find index package options
            val mainPsiFile = mainFile?.psiFile(project) ?: throw ExecutionException("Main psifile not found")
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
            val mainPsiFile = mainFile?.psiFile(project) ?: throw ExecutionException("Main psifile not found")
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