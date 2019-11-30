package nl.hannahsten.texifyidea.run.makeindex

import com.intellij.execution.ExecutionException
import com.intellij.execution.Executor
import com.intellij.execution.configurations.*
import com.intellij.execution.runners.ExecutionEnvironment
import com.intellij.openapi.application.runReadAction
import com.intellij.openapi.options.SettingsEditor
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.openapi.vfs.VirtualFile
import nl.hannahsten.texifyidea.TeXception
import nl.hannahsten.texifyidea.index.LatexCommandsIndex
import nl.hannahsten.texifyidea.run.compiler.MakeindexProgram
import nl.hannahsten.texifyidea.util.PackageUtils
import nl.hannahsten.texifyidea.util.files.psiFile
import org.jdom.Element

/**
 * Run configuration for running the makeindex tool.
 */
class MakeindexRunConfiguration(
        project: Project,
        factory: ConfigurationFactory,
        name: String
        ) : RunConfigurationBase<MakeindexCommandLineState>(project, factory, name), LocatableConfiguration {

    companion object {

        private const val PARENT_ELEMENT = "texify-makeindex"
        private const val PROGRAM = "program"
        private const val MAIN_FILE = "main-file"
        private const val WORK_DIR = "work-dir"
    }

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

    override fun readExternal(element: Element) {
        super<LocatableConfiguration>.readExternal(element)

        val parent = element.getChild(PARENT_ELEMENT)

        makeindexProgram = try {
            val programText = parent.getChildText(PROGRAM)
            if (!programText.isNullOrEmpty()) {
                MakeindexProgram.valueOf(programText)
            } else {
                MakeindexProgram.MAKEINDEX
            }
        }
        catch (e: NullPointerException) {
            MakeindexProgram.MAKEINDEX
        }

        val mainFilePath = parent.getChildText(MAIN_FILE)
        mainFile = if (mainFilePath != null) {
            LocalFileSystem.getInstance().findFileByPath(mainFilePath)
        }
        else {
            null
        }

        val workDirPath = parent.getChildText(WORK_DIR)
        workingDirectory = if (workDirPath != null) {
            LocalFileSystem.getInstance().findFileByPath(workDirPath)
        }
        else {
            null
        }
    }

    override fun writeExternal(element: Element) {
        super<LocatableConfiguration>.writeExternal(element)

        val parent = element.getChild(PARENT_ELEMENT) ?: Element(PARENT_ELEMENT).apply { element.addContent(this) }
        parent.removeContent()

        parent.addContent(Element(PROGRAM).apply{ text = makeindexProgram?.name ?: "" })
        parent.addContent(Element(MAIN_FILE).apply{ text = mainFile?.path ?: "" })
        parent.addContent(Element(WORK_DIR).apply{ text = workingDirectory?.path ?: "" })
    }

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
            val mainPsiFile = mainFile?.psiFile(project) ?: throw ExecutionException("Main file not found")
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
            val mainPsiFile = mainFile?.psiFile(project) ?: throw ExecutionException("Main file not found")
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