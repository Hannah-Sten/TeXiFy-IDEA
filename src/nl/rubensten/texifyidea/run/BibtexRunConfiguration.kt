package nl.rubensten.texifyidea.run

import com.intellij.execution.Executor
import com.intellij.execution.configurations.*
import com.intellij.execution.runners.ExecutionEnvironment
import com.intellij.openapi.options.SettingsEditor
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.openapi.vfs.VirtualFile
import nl.rubensten.texifyidea.run.compiler.BibliographyCompiler
import org.jdom.Element

/**
 * @author Sten Wessel
 */
class BibtexRunConfiguration(
        project: Project,
        factory: ConfigurationFactory,
        name: String
) : RunConfigurationBase(project, factory, name), LocatableConfiguration {

    companion object {

        private val PARENT_ELEMENT = "texify-bibtex"
        private val COMPILER = "compiler"
        private val COMPILER_PATH = "compiler-path"
        private val COMPILER_ARGUMENTS = "compiler-arguments"
        private val MAIN_FILE = "main-file"
        private val AUX_DIR = "aux-dir"
    }

    var compiler: BibliographyCompiler? = null
    var compilerPath: String? = null
    var compilerArguments: String? = null
        set(value) {
            field = value?.trim()
            field = if (field?.isEmpty() == true) null else field
        }

    var mainFile: VirtualFile? = null
    var auxDir: VirtualFile? = null

    override fun getConfigurationEditor(): SettingsEditor<out RunConfiguration> = BibtexSettingsEditor(project)

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

        val mainFilePath = parent.getChildText(MAIN_FILE)
        mainFile = if (mainFilePath != null) {
            LocalFileSystem.getInstance().findFileByPath(mainFilePath)
        }
        else {
            null
        }

        val auxDirPath = parent.getChildText(AUX_DIR)
        auxDir = if (auxDirPath != null) {
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
        parent.addContent(Element(MAIN_FILE).apply { text = mainFile?.path ?: "" })
        parent.addContent(Element(AUX_DIR).apply { text = auxDir?.path ?: "" })
    }

    override fun isGeneratedName() = name == suggestedName()

    override fun suggestedName() = mainFile?.nameWithoutExtension?.plus(" bibliography")

    fun setSuggestedName() {
        name = suggestedName()
    }
}
