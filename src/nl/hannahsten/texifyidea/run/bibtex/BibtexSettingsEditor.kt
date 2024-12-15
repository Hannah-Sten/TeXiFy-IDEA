package nl.hannahsten.texifyidea.run.bibtex

import com.intellij.execution.configuration.EnvironmentVariablesComponent
import com.intellij.openapi.fileChooser.FileChooserDescriptor
import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory
import com.intellij.openapi.options.SettingsEditor
import com.intellij.openapi.project.Project
import com.intellij.openapi.roots.ProjectRootManager
import com.intellij.openapi.ui.*
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.ui.RawCommandLineEditor
import com.intellij.ui.SeparatorComponent
import nl.hannahsten.texifyidea.run.compiler.BibliographyCompiler
import java.awt.event.ItemEvent
import javax.swing.JCheckBox
import javax.swing.JComponent
import javax.swing.JPanel

/**
 * @author Sten Wessel
 */
class BibtexSettingsEditor(private val project: Project) : SettingsEditor<BibtexRunConfiguration>() {

    private lateinit var panel: JPanel
    private lateinit var compiler: LabeledComponent<ComboBox<BibliographyCompiler>>
    private lateinit var enableCompilerPath: JCheckBox
    private lateinit var compilerPath: TextFieldWithBrowseButton
    private lateinit var compilerArguments: LabeledComponent<RawCommandLineEditor>
    private lateinit var environmentVariables: EnvironmentVariablesComponent
    private lateinit var mainFile: LabeledComponent<TextFieldWithBrowseButton>

    /** Keep track of the the working directory for bibtex, i.e., where bibtex should find the files it needs.
     * Is automatically set based on the LaTeX run config when created. */
    private lateinit var bibWorkingDir: LabeledComponent<TextFieldWithBrowseButton>

    override fun createEditor(): JComponent {
        createUIComponents()
        return panel
    }

    override fun resetEditorFrom(runConfig: BibtexRunConfiguration) {
        compiler.component.selectedItem = runConfig.compiler
        compilerPath.text = runConfig.compilerPath ?: ""
        compilerArguments.component.text = runConfig.compilerArguments ?: ""
        environmentVariables.envData = runConfig.environmentVariables
        enableCompilerPath.isSelected = runConfig.compilerPath != null
        mainFile.component.text = runConfig.mainFile?.path ?: ""
        bibWorkingDir.component.text = runConfig.bibWorkingDir?.path ?: ""
    }

    override fun applyEditorTo(runConfig: BibtexRunConfiguration) {
        runConfig.compiler = compiler.component.selectedItem as BibliographyCompiler?
        runConfig.compilerPath = if (enableCompilerPath.isSelected) compilerPath.text else null
        runConfig.compilerArguments = compilerArguments.component.text
        runConfig.environmentVariables = environmentVariables.envData
        runConfig.mainFile = if (mainFile.component.text.isNotBlank()) LocalFileSystem.getInstance().findFileByPath(mainFile.component.text) else null
        runConfig.bibWorkingDir = if (bibWorkingDir.component.text.isNotBlank()) LocalFileSystem.getInstance().findFileByPath(bibWorkingDir.component.text) else null
    }

    private fun createUIComponents() {
        panel = JPanel().apply {
            layout = VerticalFlowLayout(VerticalFlowLayout.TOP)

            // Compiler
            val compilerField = ComboBox(BibliographyCompiler.entries.toTypedArray())
            compiler = LabeledComponent.create(compilerField, "Compiler")
            add(compiler)

            // Custom compiler path
            compilerPath = TextFieldWithBrowseButton().apply {
                addBrowseFolderListener(
                    TextBrowseFolderListener(
                        FileChooserDescriptor(true, false, false, false, false, false)
                            .withFileFilter { file -> file.nameWithoutExtension == (compiler.component.selectedItem as BibliographyCompiler?)?.executableName }
                            .withTitle("Choose ${compiler.component.selectedItem} executable")
                    )
                )

                isEnabled = false
                addPropertyChangeListener("enabled") { e ->
                    if (!(e.newValue as Boolean)) {
                        this.text = ""
                    }
                }
            }

            enableCompilerPath = JCheckBox("Select custom compiler executable path (required on Mac OS X)").apply {
                addItemListener { e ->
                    compilerPath.isEnabled = e.stateChange == ItemEvent.SELECTED
                }
            }

            add(enableCompilerPath)
            add(compilerPath)

            // Custom compiler arguments
            val argumentsTitle = "Custom compiler arguments"
            val argumentsField = RawCommandLineEditor()
            compilerArguments = LabeledComponent.create(argumentsField, argumentsTitle)
            add(compilerArguments)

            environmentVariables = EnvironmentVariablesComponent()
            add(environmentVariables)

            add(SeparatorComponent())

            // Main file
            val mainFileField = TextFieldWithBrowseButton().apply {
                addBrowseFolderListener(
                    TextBrowseFolderListener(
                        FileChooserDescriptorFactory.createSingleFileDescriptor()
                            .withTitle("Choose the Main .tex File")
                            .withExtensionFilter("tex")
                            .withRoots(*ProjectRootManager.getInstance(project).contentRootsFromAllModules.toSet().toTypedArray())
                    )
                )
            }
            mainFile = LabeledComponent.create(mainFileField, "Main file that includes bibliography")
            add(mainFile)

            // Working directory
            val workingDirField = TextFieldWithBrowseButton()
            workingDirField.addBrowseFolderListener(
                TextBrowseFolderListener(
                    FileChooserDescriptor(false, true, false, false, false, false)
                        .withTitle("Choose the BibTeX Working Directory")
                        .withRoots(
                            *ProjectRootManager.getInstance(project)
                                .contentRootsFromAllModules
                        )
                )
            )
            bibWorkingDir = LabeledComponent.create(workingDirField, "Working directory for bibtex")
            add(bibWorkingDir)
        }
    }
}
