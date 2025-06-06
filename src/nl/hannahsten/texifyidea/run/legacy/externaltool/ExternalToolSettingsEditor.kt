package nl.hannahsten.texifyidea.run.legacy.externaltool

import com.intellij.execution.configuration.EnvironmentVariablesComponent
import com.intellij.openapi.fileChooser.FileChooserDescriptor
import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory
import com.intellij.openapi.options.SettingsEditor
import com.intellij.openapi.project.Project
import com.intellij.openapi.roots.ProjectRootManager
import com.intellij.openapi.ui.*
import com.intellij.openapi.vfs.LocalFileSystem
import nl.hannahsten.texifyidea.run.legacy.ExternalTool
import javax.swing.JComponent
import javax.swing.JPanel

/**
 * @author Thomas
 */
class ExternalToolSettingsEditor(private val project: Project) : SettingsEditor<ExternalToolRunConfiguration>() {

    private lateinit var panel: JPanel
    private lateinit var program: LabeledComponent<ComboBox<ExternalTool>>
    private lateinit var mainFile: LabeledComponent<TextFieldWithBrowseButton>
    private lateinit var workingDirectory: LabeledComponent<TextFieldWithBrowseButton>
    private lateinit var environmentVariables: EnvironmentVariablesComponent

    override fun createEditor(): JComponent {
        createUIComponents()
        return panel
    }

    // Initialize editor from given run config
    override fun resetEditorFrom(runConfig: ExternalToolRunConfiguration) {
        program.component.selectedItem = runConfig.program
        mainFile.component.text = runConfig.mainFile?.path ?: ""
        workingDirectory.component.text = runConfig.workingDirectory?.path ?: ""
        environmentVariables.envData = runConfig.environmentVariables
    }

    // Save user selected settings to the given run config
    override fun applyEditorTo(runConfig: ExternalToolRunConfiguration) {
        runConfig.program = program.component.selectedItem as ExternalTool
        runConfig.mainFile = LocalFileSystem.getInstance().findFileByPath(mainFile.component.text)
        runConfig.workingDirectory = LocalFileSystem.getInstance().findFileByPath(workingDirectory.component.text)
        runConfig.environmentVariables = environmentVariables.envData
    }

    private fun createUIComponents() {
        panel = JPanel().apply {
            layout = VerticalFlowLayout(VerticalFlowLayout.TOP)

            // Program
            val programField = ComboBox(ExternalTool.entries.toTypedArray())
            program = LabeledComponent.create(programField, "External tool")
            add(program)

            // Main file
            val mainFileField = TextFieldWithBrowseButton().apply {
                addBrowseFolderListener(
                    TextBrowseFolderListener(
                        FileChooserDescriptorFactory.createSingleFileDescriptor()
                            .withTitle("Choose Main LaTeX File")
                            .withExtensionFilter("tex")
                            .withRoots(*ProjectRootManager.getInstance(project).contentRootsFromAllModules.toSet().toTypedArray())
                    )
                )
            }
            mainFile = LabeledComponent.create(mainFileField, "Main file which requires the external tool")
            add(mainFile)

            // Working directory
            val workDirField = TextFieldWithBrowseButton().apply {
                addBrowseFolderListener(
                    TextBrowseFolderListener(
                        FileChooserDescriptor(false, true, false, false, false, false)
                            .withTitle("Choose Output Directory of Main LaTeX File")
                    )
                )
            }
            workingDirectory = LabeledComponent.create(workDirField, "Working directory for the external tool")
            add(workingDirectory)

            environmentVariables = EnvironmentVariablesComponent()
            add(environmentVariables)
        }
    }
}
