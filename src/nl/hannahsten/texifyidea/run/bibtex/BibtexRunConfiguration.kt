package nl.hannahsten.texifyidea.run.bibtex

import com.intellij.diagnostic.logging.LogConsoleManagerBase
import com.intellij.execution.Executor
import com.intellij.execution.configuration.EnvironmentVariablesData
import com.intellij.execution.configurations.*
import com.intellij.execution.process.ProcessHandler
import com.intellij.execution.runners.ExecutionEnvironment
import com.intellij.icons.AllIcons
import com.intellij.openapi.options.SettingsEditor
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.openapi.vfs.VirtualFile
import nl.hannahsten.texifyidea.run.bibtex.logtab.BibtexLogTabComponent
import nl.hannahsten.texifyidea.run.compiler.BibliographyCompiler
import org.jdom.Element

/**
 * @author Sten Wessel
 */
class BibtexRunConfiguration(
    project: Project,
    factory: ConfigurationFactory,
    name: String
) : RunConfigurationBase<BibtexCommandLineState>(project, factory, name), LocatableConfiguration {

    companion object {

        private const val PARENT_ELEMENT = "texify-bibtex"
        private const val COMPILER = "compiler"
        private const val COMPILER_PATH = "compiler-path"
        private const val COMPILER_ARGUMENTS = "compiler-arguments"
        private const val MAIN_FILE = "main-file"
        private const val AUX_DIR = "aux-dir"
    }

    var compiler: BibliographyCompiler? = null
    var compilerPath: String? = null
    var compilerArguments: String? = null
        set(value) {
            field = value?.trim()
            field = if (field?.isEmpty() == true) null else field
        }
    var environmentVariables: EnvironmentVariablesData = EnvironmentVariablesData.DEFAULT

    var mainFile: VirtualFile? = null
    var bibWorkingDir: VirtualFile? = null

    override fun getConfigurationEditor(): SettingsEditor<out RunConfiguration> = BibtexSettingsEditor(project)

    override fun createAdditionalTabComponents(
        manager: AdditionalTabComponentManager?,
        startedProcess: ProcessHandler?
    ) {
        super.createAdditionalTabComponents(manager, startedProcess)

        if (manager is LogConsoleManagerBase && startedProcess != null) {
            manager.addAdditionalTabComponent(BibtexLogTabComponent(project, mainFile, startedProcess), "BibTeX-Log", AllIcons.Vcs.Changelist, false)
        }
    }

    override fun checkConfiguration() {
        if (compiler == null) {
            throw RuntimeConfigurationError("No compiler specified.")
        }

        if (mainFile == null) {
            throw RuntimeConfigurationError("No main file specified.")
        }
    }

    override fun getState(executor: Executor, environment: ExecutionEnvironment): RunProfileState {
        return BibtexCommandLineState(environment, this)
    }

    override fun readExternal(element: Element) {
        super<RunConfigurationBase>.readExternal(element)

        val parent = element.getChild(PARENT_ELEMENT)

        compiler = try {
            BibliographyCompiler.valueOf(parent.getChildText(COMPILER))
        }
        catch (e: IllegalArgumentException) {
            null
        }

        compilerPath = parent.getChildText(COMPILER_PATH)
        if (compilerPath?.isEmpty() == true) {
            compilerPath = null
        }

        compilerArguments = parent.getChildText(COMPILER_ARGUMENTS)
        environmentVariables = EnvironmentVariablesData.readExternal(parent)

        val mainFilePath = parent.getChildText(MAIN_FILE)
        mainFile = if (mainFilePath.isNullOrBlank().not()) {
            LocalFileSystem.getInstance().findFileByPath(mainFilePath)
        }
        else {
            null
        }

        val auxDirPath = parent.getChildText(AUX_DIR)
        bibWorkingDir = if (auxDirPath.isNullOrBlank().not()) {
            LocalFileSystem.getInstance().findFileByPath(auxDirPath)
        }
        else {
            null
        }
    }

    override fun writeExternal(element: Element) {
        super<RunConfigurationBase>.writeExternal(element)

        val parent = element.getChild(PARENT_ELEMENT) ?: Element(PARENT_ELEMENT).apply { element.addContent(this) }
        parent.removeContent()

        parent.addContent(Element(COMPILER).apply { text = compiler?.name ?: "" })
        parent.addContent(Element(COMPILER_PATH).apply { text = compilerPath ?: "" })
        parent.addContent(Element(COMPILER_ARGUMENTS).apply { text = compilerArguments ?: "" })
        this.environmentVariables.writeExternal(parent)
        parent.addContent(Element(MAIN_FILE).apply { text = mainFile?.path ?: "" })
        parent.addContent(Element(AUX_DIR).apply { text = bibWorkingDir?.path ?: "" })
    }

    override fun isGeneratedName() = name == suggestedName()

    override fun suggestedName() = mainFile?.nameWithoutExtension.plus(" bibliography")

    fun setSuggestedName() {
        name = suggestedName()
    }
}
