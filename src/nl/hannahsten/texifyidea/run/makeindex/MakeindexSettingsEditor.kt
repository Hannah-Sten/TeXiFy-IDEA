package nl.hannahsten.texifyidea.run.makeindex

import com.intellij.openapi.fileChooser.FileChooserDescriptor
import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory
import com.intellij.openapi.options.SettingsEditor
import com.intellij.openapi.project.Project
import com.intellij.openapi.roots.ProjectRootManager
import com.intellij.openapi.ui.*
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.ui.RawCommandLineEditor
import nl.hannahsten.texifyidea.TexifyBundle
import nl.hannahsten.texifyidea.file.LatexFileType
import nl.hannahsten.texifyidea.run.compiler.MakeindexProgram
import javax.swing.JComponent
import javax.swing.JPanel

/**
 * @author Thomas Schouten
 */
class MakeindexSettingsEditor(private val project: Project) : SettingsEditor<MakeindexRunConfiguration>() {

    private lateinit var panel: JPanel
    private lateinit var makeindexProgram: LabeledComponent<ComboBox<MakeindexProgram>>
    private lateinit var mainFile: LabeledComponent<TextFieldWithBrowseButton>
    private lateinit var commandLineArguments: LabeledComponent<RawCommandLineEditor>
    private lateinit var workingDirectory: LabeledComponent<TextFieldWithBrowseButton>

    override fun createEditor(): JComponent {
        createUIComponents()
        return panel
    }

    // Initialize editor from given run config
    override fun resetEditorFrom(runConfig: MakeindexRunConfiguration) {
        makeindexProgram.component.selectedItem = runConfig.makeindexProgram
        mainFile.component.text = runConfig.mainFile?.path ?: ""
        commandLineArguments.component.text = runConfig.commandLineArguments
        workingDirectory.component.text = runConfig.workingDirectory?.path ?: ""
    }

    // Save user selected settings to the given run config
    override fun applyEditorTo(runConfig: MakeindexRunConfiguration) {
        runConfig.makeindexProgram = makeindexProgram.component.selectedItem as MakeindexProgram
        runConfig.mainFile = if (mainFile.component.text.isNotBlank()) LocalFileSystem.getInstance().findFileByPath(mainFile.component.text) else null
        runConfig.commandLineArguments = commandLineArguments.component.text
        runConfig.workingDirectory = if (workingDirectory.component.text.isNotBlank()) LocalFileSystem.getInstance().findFileByPath(workingDirectory.component.text) else null
    }

    private fun createUIComponents() {
        panel = JPanel().apply {
            layout = VerticalFlowLayout(VerticalFlowLayout.TOP)

            // Program
            val programField = ComboBox(MakeindexProgram.entries.toTypedArray())
            makeindexProgram = LabeledComponent.create(programField, TexifyBundle.message("run.makeindex.settings.index.program"))
            add(makeindexProgram)

            // Main file
            val mainFileField = TextFieldWithBrowseButton().apply {
                addBrowseFolderListener(
                    TextBrowseFolderListener(
                        FileChooserDescriptorFactory.createSingleFileDescriptor(LatexFileType)
                            .withTitle(TexifyBundle.message("run.makeindex.settings.choose.main.file"))
                            .withExtensionFilter("tex")
                            .withRoots(*ProjectRootManager.getInstance(project).contentRootsFromAllModules.toSet().toTypedArray())
                    )
                )
            }
            mainFile = LabeledComponent.create(mainFileField, TexifyBundle.message("run.makeindex.settings.main.file"))
            add(mainFile)

            commandLineArguments = LabeledComponent.create(RawCommandLineEditor(), TexifyBundle.message("run.makeindex.settings.custom.arguments"))
            add(commandLineArguments)

            // Working directory
            val workDirField = TextFieldWithBrowseButton().apply {
                addBrowseFolderListener(
                    TextBrowseFolderListener(
                        FileChooserDescriptor(false, true, false, false, false, false)
                            .withTitle(TexifyBundle.message("run.makeindex.settings.choose.working.directory"))
                    )
                )
            }
            workingDirectory = LabeledComponent.create(workDirField, TexifyBundle.message("run.makeindex.settings.working.directory"))
            add(workingDirectory)
        }
    }
}
