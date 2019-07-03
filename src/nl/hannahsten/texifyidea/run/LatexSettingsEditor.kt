package nl.hannahsten.texifyidea.run

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
import nl.hannahsten.texifyidea.run.compiler.LatexCompiler
import nl.hannahsten.texifyidea.run.compiler.LatexCompiler.Format
import nl.hannahsten.texifyidea.util.LatexDistribution
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
    private val extensionSeparator = TitledSeparator("Extensions")
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
        // Make sure to use the output formats relevant for the chosen compiler
        if (runConfiguration.compiler != null) {
            outputFormat.component.removeAllItems()
            for (item in runConfiguration.compiler!!.outputFormats) {
                outputFormat.component.addItem(item)
            }
            if (runConfiguration.compiler!!.outputFormats.contains(runConfiguration.outputFormat)) {
                outputFormat.component.selectedItem = runConfiguration.outputFormat
            }
            else {
                outputFormat.component.selectedItem = Format.PDF
            }
        }

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

        // Remove bibtex run config when switching to a compiler which includes running bibtex
        if (runConfiguration.compiler?.includesBibtex == true) {
            runConfiguration.bibRunConfig = null
            extensionSeparator.isVisible = false
            bibliographyPanel.isVisible = false
        }
        else {
            extensionSeparator.isVisible = true
            bibliographyPanel.isVisible = true

            // Apply bibliography, only if not hidden
            runConfiguration.bibRunConfig = bibliographyPanel.configuration
        }

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
        val format = outputFormat.component.selectedItem as Format?
        runConfiguration.outputFormat = format ?: Format.PDF
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

        // The aux directory is only available on MiKTeX, so only allow disabling on MiKTeX
        if (LatexDistribution.isMiktex) {
            panel.add(TitledSeparator("Options"))

            // Auxiliary files
            auxDir = JBCheckBox("Separate auxiliary files from output")
            auxDir!!.isSelected = true
            panel.add(auxDir)
        }

        // Output folder
        outDir = JBCheckBox("Separate output files from source " + "(disable this when using BiBTeX without MiKTeX)")
        // Enable by default.
        outDir!!.isSelected = true
        panel.add(outDir)

        // Output format.
        val selectedCompiler = compiler.component.selectedItem as LatexCompiler
        val cboxFormat = ComboBox(selectedCompiler.outputFormats)
        outputFormat = LabeledComponent.create<ComboBox<Format>>(cboxFormat, "Output format")
        outputFormat.setSize(128, outputFormat.height)
        panel.add(outputFormat)

        panel.add(extensionSeparator)

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
