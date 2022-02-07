package nl.hannahsten.texifyidea.run.makeindex

import com.intellij.execution.Executor
import com.intellij.execution.configurations.*
import com.intellij.execution.runners.ExecutionEnvironment
import com.intellij.openapi.options.SettingsEditor
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.openapi.vfs.VirtualFile
import nl.hannahsten.texifyidea.run.compiler.MakeindexProgram
import nl.hannahsten.texifyidea.run.latex.getMakeindexOptions
import org.jdom.Element
import java.util.*

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
        val makeindexOptions = getMakeindexOptions(mainFile, project)
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
        catch (ignored: NullPointerException) {
        }

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

    override fun suggestedName() = mainFile?.nameWithoutExtension + " " + makeindexProgram.name.lowercase(Locale.getDefault())

    fun setSuggestedName() {
        name = suggestedName()
    }
}