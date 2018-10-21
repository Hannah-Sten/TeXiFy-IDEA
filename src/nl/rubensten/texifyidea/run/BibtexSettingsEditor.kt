package nl.rubensten.texifyidea.run

import com.intellij.openapi.fileChooser.FileChooserDescriptor
import com.intellij.openapi.fileChooser.FileTypeDescriptor
import com.intellij.openapi.options.SettingsEditor
import com.intellij.openapi.project.Project
import com.intellij.openapi.roots.ProjectRootManager
import com.intellij.openapi.ui.*
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.ui.RawCommandLineEditor
import com.intellij.ui.SeparatorComponent
import nl.rubensten.texifyidea.run.compiler.BibliographyCompiler
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
    private lateinit var mainFile: LabeledComponent<TextFieldWithBrowseButton>
    private lateinit var auxFile: LabeledComponent<TextFieldWithBrowseButton>

    override fun createEditor(): JComponent {
        createUIComponents();
        return panel;
    }

    override fun resetEditorFrom(runConfig: BibtexRunConfiguration) {
        compiler.component.selectedItem = runConfig.compiler
        compilerPath.text = runConfig.compilerPath ?: ""
        compilerArguments.component.text = runConfig.compilerArguments ?: ""
        enableCompilerPath.isSelected = runConfig.compilerPath != null
        mainFile.component.text = runConfig.mainFile?.path ?: ""
        auxFile.component.text = runConfig.bibWorkingDirectory?.path ?: ""
    }

    override fun applyEditorTo(runConfig: BibtexRunConfiguration) {
        runConfig.compiler = compiler.component.selectedItem as BibliographyCompiler?
        runConfig.compilerPath = if (enableCompilerPath.isSelected) compilerPath.text else null
        runConfig.compilerArguments = compilerArguments.component.text
        runConfig.mainFile = LocalFileSystem.getInstance().findFileByPath(mainFile.component.text)
        runConfig.bibWorkingDirectory = LocalFileSystem.getInstance().findFileByPath(auxFile.component.text)
    }

    private fun createUIComponents() {
        panel = JPanel().apply {
            layout = VerticalFlowLayout(VerticalFlowLayout.TOP)

            // Compiler
            val compilerField = ComboBox<BibliographyCompiler>(BibliographyCompiler.values())
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
            val argumentsField = RawCommandLineEditor().apply { dialogCaption = argumentsTitle }
            compilerArguments = LabeledComponent.create(argumentsField, argumentsTitle)
            add(compilerArguments)

            add(SeparatorComponent())

            // Main file
            val mainFileField = TextFieldWithBrowseButton().apply { addBrowseFolderListener(
                TextBrowseFolderListener(
                    FileTypeDescriptor("Choose the main .tex file", ".tex")
                        .withRoots(*ProjectRootManager.getInstance(project).contentRootsFromAllModules)
                )
            ) }
            mainFile = LabeledComponent.create(mainFileField, "Main file that includes bibliography")
            add(mainFile)

            // Aux file
            val auxFileField = TextFieldWithBrowseButton().apply { addBrowseFolderListener(
                TextBrowseFolderListener(
                    FileChooserDescriptor(false, true, false, false, false, false)
                        .withRoots(*ProjectRootManager.getInstance(project).contentRootsFromAllModules)
                )
            ) }
            auxFile = LabeledComponent.create(auxFileField, "Auxiliary files directory (keep empty when location is the same as for the main file)")
            add(auxFile)
        }
    }
}
