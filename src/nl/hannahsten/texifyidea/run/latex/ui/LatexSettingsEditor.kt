package nl.hannahsten.texifyidea.run.latex.ui

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
import nl.hannahsten.texifyidea.run.bibtex.BibtexRunConfigurationType
import nl.hannahsten.texifyidea.run.compiler.LatexCompiler
import nl.hannahsten.texifyidea.run.compiler.LatexCompiler.Format
import nl.hannahsten.texifyidea.run.compiler.LatexCompiler.PDFLATEX
import nl.hannahsten.texifyidea.run.latex.LatexRunConfiguration
import nl.hannahsten.texifyidea.run.makeindex.MakeindexRunConfigurationType
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
    private lateinit var outputPath: LabeledComponent<ComponentWithBrowseButton<*>>
    // Not shown on non-MiKTeX systems
    private var auxilPath: LabeledComponent<ComponentWithBrowseButton<*>>? = null

    // The following options may or may not exist.
    private var compileTwice: JBCheckBox? = null
    private lateinit var outputFormat: LabeledComponent<ComboBox<Format>>
    private val extensionSeparator = TitledSeparator("Extensions")
    private lateinit var bibliographyPanel: RunConfigurationPanel<BibtexRunConfigurationType>
    private lateinit var makeindexPanel: RunConfigurationPanel<MakeindexRunConfigurationType>

    /** Whether to enable the sumatraPath text field. */
    private lateinit var enableSumatraPath: JBCheckBox

    /** Allow users to specify a custom path to SumatraPDF.  */
    private lateinit var sumatraPath: TextFieldWithBrowseButton

    /** Whether to enable the custom pdf viewer command text field. */
    private lateinit var enableViewerCommand: JBCheckBox

    /** Allow users to specify a custom pdf viewer command. */
    private lateinit var viewerCommand: JBTextField

    // Discard all non-confirmed user changes made via the UI
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
        txtFile.text = runConfiguration.mainFile?.path ?: ""

        if (auxilPath != null) {
            val auxilPathTextField = auxilPath!!.component as TextFieldWithBrowseButton
            auxilPathTextField.text = runConfiguration.auxilPath?.path ?: ""
        }

        val outputPathTextField = outputPath.component as TextFieldWithBrowseButton
        outputPathTextField.text = runConfiguration.outputPath?.path ?: ""

        // Reset whether to compile twice
        if (compileTwice != null) {
            if (runConfiguration.compiler?.handlesNumberOfCompiles == true) {
                compileTwice!!.isVisible = false
                runConfiguration.compileTwice = false
            }
            else {
                compileTwice!!.isVisible = true
            }
            compileTwice!!.isSelected = runConfiguration.compileTwice

            // If we need to compile twice, make sure the LatexCommandLineState knows
            if (runConfiguration.compileTwice) {
                runConfiguration.isLastRunConfig = false
            }
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
        bibliographyPanel.configurations = runConfiguration.bibRunConfigs.filterNotNull().toMutableSet()

        // Reset makeindex
        makeindexPanel.configurations = if (runConfiguration.makeindexRunConfig != null) mutableSetOf(runConfiguration.makeindexRunConfig!!) else mutableSetOf()
    }

    // Confirm the changes, i.e. copy current UI state into the target settings object.
    @Throws(ConfigurationException::class)
    override fun applyEditorTo(runConfiguration: LatexRunConfiguration) {
        // Apply chosen compiler.
        val chosenCompiler = compiler.component.selectedItem as? LatexCompiler ?: PDFLATEX
        runConfiguration.compiler = chosenCompiler

        // Remove bibtex run config when switching to a compiler which includes running bibtex
        if (runConfiguration.compiler?.includesBibtex == true) {
            runConfiguration.bibRunConfigs = setOf()
            bibliographyPanel.isVisible = false
        }
        else {
            bibliographyPanel.isVisible = true

            // Apply bibliography, only if not hidden
            runConfiguration.bibRunConfigs = bibliographyPanel.configurations
        }

        if (runConfiguration.compiler?.includesMakeindex == true) {
            runConfiguration.makeindexRunConfig = null
            makeindexPanel.isVisible = false
        }
        else {
            makeindexPanel.isVisible = true

            // Apply makeindex
            // For now we just run the first one, this can be extended to run all of them but that requires some extra work
            runConfiguration.makeindexRunConfig = makeindexPanel.configurations.firstOrNull()
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

        val outputPathTextField = outputPath.component as TextFieldWithBrowseButton
        runConfiguration.setFileOutputPath(outputPathTextField.text)

        if (auxilPath != null) {
            val auxilPathTextField = auxilPath!!.component as TextFieldWithBrowseButton
            runConfiguration.setFileAuxilPath(auxilPathTextField.text)
        }

        if (compileTwice != null) {
            // Only show option to configure number of compiles when applicable
            if (runConfiguration.compiler?.handlesNumberOfCompiles == true) {
                compileTwice!!.isVisible = false
                runConfiguration.compileTwice = false
            }
            else {
                compileTwice!!.isVisible = true
                runConfiguration.compileTwice = compileTwice!!.isSelected
            }

            // If we need to compile twice, make sure the LatexCommandLineState knows
            if (runConfiguration.compileTwice) {
                runConfiguration.isLastRunConfig = false
            }
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

        addOutputPathField(panel)

        compileTwice = JBCheckBox("Always compile twice")
        compileTwice!!.isSelected = false
        panel.add(compileTwice)

        // Output format.
        val selectedCompiler = compiler.component.selectedItem as LatexCompiler
        val cboxFormat = ComboBox(selectedCompiler.outputFormats)
        outputFormat = LabeledComponent.create<ComboBox<Format>>(cboxFormat, "Output format")
        outputFormat.setSize(128, outputFormat.height)
        panel.add(outputFormat)

        panel.add(extensionSeparator)

        // Extension panels
        bibliographyPanel = RunConfigurationPanel(project!!,"Bibliography: ", BibtexRunConfigurationType::class.java)
        panel.add(bibliographyPanel)

        makeindexPanel = RunConfigurationPanel(project!!, "Makeindex: ", MakeindexRunConfigurationType::class.java)
        panel.add(makeindexPanel)
    }

    private fun addOutputPathField(panel: JPanel) {
        // The aux directory is only available on MiKTeX, so only allow disabling on MiKTeX
        if (LatexDistribution.isMiktex) {

            val auxilPathField = TextFieldWithBrowseButton()
            auxilPathField.addBrowseFolderListener(
                    TextBrowseFolderListener(
                            FileChooserDescriptor(false, true, false, false, false, false)
                                    .withTitle("Choose a directory for auxiliary files")
                                    .withRoots(*ProjectRootManager.getInstance(project!!)
                                            .contentRootsFromAllModules)
                    )
            )
            auxilPath = LabeledComponent.create(auxilPathField, "Directory for auxiliary files")
            panel.add(auxilPath)
        }

        val outputPathField = TextFieldWithBrowseButton()
        outputPathField.addBrowseFolderListener(
                TextBrowseFolderListener(
                        FileChooserDescriptor(false, true, false, false, false, false)
                                .withTitle("Choose a directory for output files")
                                .withRoots(*ProjectRootManager.getInstance(project!!)
                                        .contentRootsFromAllModules)
                )
        )
        outputPath = LabeledComponent.create(outputPathField, "Directory for output files (use directory of main file when using BiBTeX without MiKTeX)")
        panel.add(outputPath)
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
