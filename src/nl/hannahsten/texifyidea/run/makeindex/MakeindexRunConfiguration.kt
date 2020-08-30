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
import nl.hannahsten.texifyidea.index.LatexCommandsIndex
import nl.hannahsten.texifyidea.lang.Package
import nl.hannahsten.texifyidea.run.compiler.MakeindexProgram
import nl.hannahsten.texifyidea.util.Magic
import nl.hannahsten.texifyidea.util.PackageUtils
import nl.hannahsten.texifyidea.util.files.psiFile
import nl.hannahsten.texifyidea.util.includedPackages
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
    var makeindexProgram: MakeindexProgram = MakeindexProgram.MAKEINDEX
    var mainFile: VirtualFile? = null
    var workingDirectory: VirtualFile? = null

    override fun getState(executor: Executor, environment: ExecutionEnvironment): RunProfileState {
        val makeindexOptions = getMakeindexOptions()
        return MakeindexCommandLineState(environment, mainFile, workingDirectory, makeindexOptions, makeindexProgram)
    }

    override fun getConfigurationEditor(): SettingsEditor<out RunConfiguration> = MakeindexSettingsEditor(project)

    override fun readExternal(element: Element) {
        super<LocatableConfiguration>.readExternal(element)

        val parent = element.getChild(PARENT_ELEMENT)

        try {
            val programText = parent.getChildText(PROGRAM)
            if (!programText.isNullOrEmpty()) {
                makeindexProgram = MakeindexProgram.valueOf(programText)
            }
        }
        catch (ignored: NullPointerException) {}

        val mainFilePath = try {
            parent.getChildText(MAIN_FILE)
        }
        catch (e: NullPointerException) {
            null
        }
        mainFile = if (!mainFilePath.isNullOrBlank()) {
            LocalFileSystem.getInstance().findFileByPath(mainFilePath)
        }
        else {
            null
        }

        val workDirPath = try {
            parent.getChildText(WORK_DIR)
        }
        catch (e: NullPointerException) {
            null
        }
        workingDirectory = if (!workDirPath.isNullOrBlank()) {
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

        parent.addContent(Element(PROGRAM).apply { text = makeindexProgram.name })
        parent.addContent(Element(MAIN_FILE).apply { text = mainFile?.path ?: "" })
        parent.addContent(Element(WORK_DIR).apply { text = workingDirectory?.path ?: "" })
    }

    override fun isGeneratedName() = name == suggestedName()

    override fun suggestedName() = mainFile?.nameWithoutExtension.plus(" index")

    fun setSuggestedName() {
        name = suggestedName()
    }

    /**
     * Try to find out which index program the user wants to use, based on the given options.
     * Will change [makeindexProgram] even if already set.
     */
    fun setDefaultMakeindexProgram() {
        val indexPackageOptions = getIndexPackageOptions()
        val makeindexOptions = getMakeindexOptions()

        val usedPackages = runReadAction {
            mainFile?.psiFile(project)?.includedPackages() ?: emptySet()
        }

        var indexProgram = if (usedPackages.intersect(Magic.Package.index).isNotEmpty()) {
            if (indexPackageOptions.contains("xindy")) MakeindexProgram.XINDY else MakeindexProgram.MAKEINDEX
        }
        else {
            // todo makeglossaries-lite
            if (Package.GLOSSARIES.name in usedPackages) MakeindexProgram.MAKEGLOSSARIES else if (Package.GLOSSARIESEXTRA.name in usedPackages && "record" in indexPackageOptions) MakeindexProgram.BIB2GLS else MakeindexProgram.MAKEGLOSSARIESLITE
        }


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

        makeindexProgram = indexProgram
    }

    /**
     * Get package options for included index packages.
     */
    private fun getIndexPackageOptions(): List<String> {
        return runReadAction {
            // Find index package options
            val mainPsiFile = mainFile?.psiFile(project) ?: throw ExecutionException("Main file not found")
            LatexCommandsIndex.getItemsInFileSet(mainPsiFile)
                    .filter { it.commandToken.text in PackageUtils.PACKAGE_COMMANDS }
                    .filter { command -> command.requiredParameters.any { it in Magic.Package.index  || it in Magic.Package.glossary } }
                    .flatMap { it.optionalParameters.keys }
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
                    .forEach {
                        makeindexOptions.putAll(it.optionalParameters)
                    }
            makeindexOptions
        }
    }
}