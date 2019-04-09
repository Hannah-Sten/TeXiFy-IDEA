package nl.rubensten.texifyidea.run

import com.intellij.openapi.fileChooser.FileChooserDescriptor
import com.intellij.openapi.fileChooser.FileTypeDescriptor
import com.intellij.openapi.options.ConfigurationException
import com.intellij.openapi.options.SettingsEditor
import com.intellij.openapi.project.Project
import com.intellij.openapi.roots.ProjectRootManager
import com.intellij.openapi.ui.*
import com.intellij.openapi.util.SystemInfo
import com.intellij.ui.RawCommandLineEditor
import com.intellij.ui.SeparatorComponent
import com.intellij.ui.TitledSeparator
import nl.rubensten.texifyidea.run.LatexCompiler.Format
import java.awt.event.ItemEvent
import javax.swing.JCheckBox
import javax.swing.JComponent
import javax.swing.JPanel

/**
 * @author Sten Wessel
 */
class LatexSettingsEditor(private var project: Project?) : SettingsEditor<LatexRunConfiguration>() {

    private lateinit var panel: JPanel
    private lateinit var compiler: LabeledComponent<ComboBox<LatexCompiler>>
    private lateinit var enableCompilerPath: JCheckBox
    private lateinit var compilerPath: TextFieldWithBrowseButton
    private lateinit var compilerArguments: LabeledComponent<RawCommandLineEditor>
    private lateinit var mainFile: LabeledComponent<ComponentWithBrowseButton<*>>

    // The following options may or may not exist.
    private var auxDir: JCheckBox? = null
    private var outDir: JCheckBox? = null
    private lateinit var outputFormat: LabeledComponent<ComboBox<LatexCompiler.Format>>
    private lateinit var bibliographyPanel: BibliographyPanel

    override fun resetEditorFrom(runConfiguration: LatexRunConfiguration) {
        // Reset the selected compiler.
        compiler.component.selectedItem = runConfiguration.compiler

        // Reset the custom compiler path
        compilerPath.text = runConfiguration.compilerPath
        enableCompilerPath.isSelected = runConfiguration.compilerPath != null

        // Reset compiler arguments
        val args = runConfiguration.compilerArguments
        compilerArguments.component.text = args ?: ""

        // Reset the main file to compile.
        val txtFile = mainFile.component as TextFieldWithBrowseButton
        val virtualFile = runConfiguration.mainFile
        val path = virtualFile?.path ?: ""
        txtFile.text = path

        // Reset seperate auxiliary files.
        if (auxDir != null) {
            auxDir!!.isSelected = runConfiguration.hasAuxiliaryDirectories()
        }

        // Reset seperate output files.
        if (outDir != null) {
            outDir!!.isSelected = runConfiguration.hasOutputDirectories()
        }

        // Reset output format.
        outputFormat.component.selectedItem = runConfiguration.outputFormat

        // Reset project.
        project = runConfiguration.project

        // Reset bibliography
        bibliographyPanel.configuration = runConfiguration.bibRunConfig
    }

    @Throws(ConfigurationException::class)
    override fun applyEditorTo(runConfiguration: LatexRunConfiguration) {
        // Apply chosen compiler.
        val chosenCompiler = compiler.component.selectedItem as LatexCompiler
        runConfiguration.compiler = chosenCompiler

        // Apply custom compiler path if applicable
        runConfiguration.compilerPath = if (enableCompilerPath.isSelected) compilerPath.text else null

        // Apply custom compiler arguments
        runConfiguration.compilerArguments = compilerArguments.component.text

        // Apply main file.
        val txtFile = mainFile.component as TextFieldWithBrowseButton
        val filePath = txtFile.text
        runConfiguration.setMainFile(filePath)

        // Apply auxiliary files, only if the option exists.
        if (auxDir != null) {
            val auxDirectories = auxDir!!.isSelected
            runConfiguration.setAuxiliaryDirectories(auxDirectories)
        }

        if (outDir != null) {
            val outDirectories = outDir!!.isSelected
            runConfiguration.setOutputDirectories(outDirectories)
        }

        // Apply output format.
        val format = outputFormat.component.selectedItem as Format
        runConfiguration.outputFormat = format

        // Apply bibliography
        runConfiguration.bibRunConfig = bibliographyPanel.configuration
    }

    override fun createEditor(): JComponent {
        createUIComponents()
        return panel
    }

    private fun createUIComponents() {
        // Layout
        panel = JPanel()
        panel.layout = VerticalFlowLayout(VerticalFlowLayout.TOP)

        // Compiler
        val compilerField = ComboBox(LatexCompiler.values())
        compiler = LabeledComponent.create<ComboBox<LatexCompiler>>(compilerField, "Compiler")
        panel.add(compiler)

        // Optional custom path for compiler executable
        enableCompilerPath = JCheckBox("Select custom compiler executable path (required on Mac OS X)")
        panel.add(enableCompilerPath)

        compilerPath = TextFieldWithBrowseButton()
        compilerPath.addBrowseFolderListener(
                TextBrowseFolderListener(
                        FileChooserDescriptor(true, false, false, false, false, false)
                                .withFileFilter { virtualFile -> virtualFile.nameWithoutExtension == (compilerField.selectedItem as LatexCompiler).executableName }
                                .withTitle("Choose " + compilerField.selectedItem + " executable")
                )
        )
        compilerPath.isEnabled = false
        compilerPath.addPropertyChangeListener("enabled") { e ->
            if (!(e.newValue as Boolean)) {
                compilerPath.setText(null)
            }
        }
        enableCompilerPath.addItemListener { e -> compilerPath.isEnabled = e.stateChange == ItemEvent.SELECTED }

        panel.add(compilerPath)

        // Optional custom compiler arguments
        val argumentsTitle = "Custom compiler arguments"
        val argumentsField = RawCommandLineEditor()
        argumentsField.dialogCaption = argumentsTitle

        compilerArguments = LabeledComponent.create(argumentsField, argumentsTitle)
        panel.add(compilerArguments)

        panel.add(SeparatorComponent())

        // Main file selection
        val mainFileField = TextFieldWithBrowseButton()
        mainFileField.addBrowseFolderListener(TextBrowseFolderListener(
                FileTypeDescriptor("Choose a file to compile", ".tex")
                        .withRoots(*ProjectRootManager.getInstance(project!!)
                                .contentRootsFromAllModules)
        ))
        mainFile = LabeledComponent.create(mainFileField, "Main file to compile")
        panel.add(mainFile)

        // Only add options to disable aux and out folder on Windows.
        // (Disabled on other systems by default.)
        if (SystemInfo.isWindows) {
            panel.add(TitledSeparator("Options"))

            // Auxiliary files
            auxDir = JCheckBox("Separate auxiliary files from output (MiKTeX only)")
            // Only enable by default on Windows.
            auxDir!!.isSelected = SystemInfo.isWindows
            panel.add(auxDir)
        }

        // Output folder
        outDir = JCheckBox("Separate output files from source " + "(disable this when using BiBTeX without MiKTeX)")
        // Enable by default.
        outDir!!.isSelected = true
        panel.add(outDir)

        // Output format.
        val cboxFormat = ComboBox(Format.values())
        outputFormat = LabeledComponent.create<ComboBox<LatexCompiler.Format>>(cboxFormat, "Output format")
        outputFormat.setSize(128, outputFormat.height)
        panel.add(outputFormat)

        panel.add(TitledSeparator("Extensions"))

        // Extension panels
        bibliographyPanel = BibliographyPanel(project!!)
        panel.add(bibliographyPanel)
    }
}
