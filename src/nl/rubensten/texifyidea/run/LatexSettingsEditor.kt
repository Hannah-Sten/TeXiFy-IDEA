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
import com.intellij.ui.components.JBCheckBox
import com.intellij.ui.components.JBTextField
import nl.rubensten.texifyidea.run.LatexCompiler.Format
import java.awt.event.ItemEvent
import javax.swing.JComponent
import javax.swing.JPanel

/**
 * @author Sten Wessel
 */
class LatexSettingsEditor(private var project: Project?) : SettingsEditor<LatexRunConfiguration>() {

    private lateinit var panel: JPanel
    private lateinit var compiler: LabeledComponent<ComboBox<LatexCompiler>>
    private lateinit var enableCompilerPath: JBCheckBox
    private lateinit var compilerPath: TextFieldWithBrowseButton
    private lateinit var compilerArguments: LabeledComponent<RawCommandLineEditor>
    private lateinit var mainFile: LabeledComponent<ComponentWithBrowseButton<*>>

    // The following options may or may not exist.
    private var auxDir: JBCheckBox? = null
    private var outDir: JBCheckBox? = null
    private lateinit var outputFormat: LabeledComponent<ComboBox<Format>>
    private lateinit var bibliographyPanel: BibliographyPanel

    /** Whether to enable the sumatraPath text field. */
    private lateinit var enableSumatraPath: JBCheckBox

    /** Allow users to specify a custom path to SumatraPDF.  */
    private lateinit var sumatraPath: TextFieldWithBrowseButton

    /** Whether to enable the custom pdf viewer command text field. */
    private lateinit var enableViewerCommand: JBCheckBox

    /** Allow users to specify a custom pdf viewer command. */
    private lateinit var viewerCommand: JBTextField

    override fun resetEditorFrom(runConfiguration: LatexRunConfiguration) {
        // Reset the selected compiler.
        compiler.component.selectedItem = runConfiguration.compiler

        // Reset the custom compiler path
        compilerPath.text = runConfiguration.compilerPath ?: ""
        enableCompilerPath.isSelected = runConfiguration.compilerPath != null

        if (::sumatraPath.isInitialized) {
            // Reset the custom SumatraPDF path
            sumatraPath.text = runConfiguration.sumatraPath ?: ""
            enableSumatraPath.isSelected = runConfiguration.sumatraPath != null
        }

        // Reset the pdf viewer command
        viewerCommand.text = runConfiguration.viewerCommand ?: ""
        enableViewerCommand.isSelected = runConfiguration.viewerCommand != null

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
            auxDir!!.isSelected = runConfiguration.hasAuxiliaryDirectories
        }

        // Reset seperate output files.
        if (outDir != null) {
            outDir!!.isSelected = runConfiguration.hasOutputDirectories
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

        if (::sumatraPath.isInitialized) {
            // Apply custom SumatraPDF path if applicable
            runConfiguration.sumatraPath = if (enableSumatraPath.isSelected) sumatraPath.text else null
        }

        // Apply custom pdf viewer command
        runConfiguration.viewerCommand = if (enableViewerCommand.isSelected) viewerCommand.text else null

        // Apply custom compiler arguments
        runConfiguration.compilerArguments = compilerArguments.component.text

        // Apply main file.
        val txtFile = mainFile.component as TextFieldWithBrowseButton
        val filePath = txtFile.text
        runConfiguration.setMainFile(filePath)

        // Apply auxiliary files, only if the option exists.
        if (auxDir != null) {
            val auxDirectories = auxDir!!.isSelected
            runConfiguration.hasAuxiliaryDirectories = auxDirectories
        }

        if (outDir != null) {
            val outDirectories = outDir!!.isSelected
            runConfiguration.hasOutputDirectories = outDirectories
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

        addCompilerPathField(panel)

        addSumatraPathField(panel)

        addPdfViewerCommandField(panel)

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
            auxDir = JBCheckBox("Separate auxiliary files from output (MiKTeX only)")
            // Only enable by default on Windows.
            auxDir!!.isSelected = SystemInfo.isWindows
            panel.add(auxDir)
        }

        // Output folder
        outDir = JBCheckBox("Separate output files from source " + "(disable this when using BiBTeX without MiKTeX)")
        // Enable by default.
        outDir!!.isSelected = true
        panel.add(outDir)

        // Output format.
        val cboxFormat = ComboBox(Format.values())
        outputFormat = LabeledComponent.create<ComboBox<Format>>(cboxFormat, "Output format")
        outputFormat.setSize(128, outputFormat.height)
        panel.add(outputFormat)

        panel.add(TitledSeparator("Extensions"))

        // Extension panels
        bibliographyPanel = BibliographyPanel(project!!)
        panel.add(bibliographyPanel)
    }

    /**
     * Compiler with optional custom path for compiler executable.
     */
    private fun addCompilerPathField(panel: JPanel) {
        // Compiler
        val compilerField = ComboBox(LatexCompiler.values())
        compiler = LabeledComponent.create<ComboBox<LatexCompiler>>(compilerField, "Compiler")
        panel.add(compiler)

        enableCompilerPath = JBCheckBox("Select custom compiler executable path (required on Mac OS X)")
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
    }

    /**
     * Optional custom path for SumatraPDF.
     */
    private fun addSumatraPathField(panel: JPanel) {
        if (SystemInfo.isWindows) {
            enableSumatraPath = JBCheckBox("Select custom path to SumatraPDF")
            panel.add(enableSumatraPath)


            sumatraPath = TextFieldWithBrowseButton().apply {
                addBrowseFolderListener(
                        TextBrowseFolderListener(
                                FileChooserDescriptor(false, true, false, false, false, false)
                                        .withTitle("Choose the folder which contains SumatraPDF.exe")
                        )
                )

                isEnabled = false

                addPropertyChangeListener("enabled") { e ->
                    if (!(e.newValue as Boolean)) {
                        this.setText(null)
                    }
                }
            }

            enableSumatraPath.addItemListener { e -> sumatraPath.isEnabled = e.stateChange == ItemEvent.SELECTED }

            panel.add(sumatraPath)
        }
    }

    /**
     * Optional custom pdf viewer command text field.
     */
    private fun addPdfViewerCommandField(panel: JPanel) {
        enableViewerCommand = JBCheckBox("Select custom PDF viewer command, using {pdf} for the pdf file if not the last argument")
        panel.add(enableViewerCommand)

        viewerCommand = JBTextField().apply {
            isEnabled = false
            addPropertyChangeListener("enabled") { e ->
                if (!(e.newValue as Boolean)) {
                    this.text = null
                }
            }
        }

        enableViewerCommand.addItemListener { e -> viewerCommand.isEnabled = e.stateChange == ItemEvent.SELECTED }

        panel.add(viewerCommand)

    }

}
