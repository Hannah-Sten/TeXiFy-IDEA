package nl.hannahsten.texifyidea.run.ui

import com.intellij.execution.RunnerAndConfigurationSettings
import com.intellij.execution.configuration.EnvironmentVariablesComponent
import com.intellij.execution.impl.RunnerAndConfigurationSettingsImpl
import com.intellij.execution.ui.*
import com.intellij.execution.ui.CommonParameterFragments.setMonospaced
import com.intellij.ide.DataManager
import com.intellij.ide.macro.MacrosDialog
import com.intellij.ide.macro.ProjectFileDirMacro
import com.intellij.openapi.actionSystem.PlatformDataKeys
import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory
import com.intellij.openapi.fileChooser.FileTypeDescriptor
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.ComboBox
import com.intellij.openapi.ui.LabeledComponent
import com.intellij.openapi.ui.TextComponentAccessor
import com.intellij.openapi.ui.TextFieldWithBrowseButton
import com.intellij.ui.RawCommandLineEditor
import com.intellij.ui.components.fields.ExtendableTextField
import com.intellij.util.ui.JBDimension
import nl.hannahsten.texifyidea.run.LatexRunConfiguration
import nl.hannahsten.texifyidea.run.compiler.latex.LatexCompiler
import nl.hannahsten.texifyidea.run.compiler.latex.SupportedLatexCompiler
import nl.hannahsten.texifyidea.run.options.LatexRunConfigurationAbstractPathOption
import nl.hannahsten.texifyidea.run.options.LatexRunConfigurationPathOption
import nl.hannahsten.texifyidea.run.ui.compiler.CompilerEditor
import nl.hannahsten.texifyidea.run.step.LatexCompileStep
import nl.hannahsten.texifyidea.util.magic.CompilerMagic
import java.awt.BorderLayout
import java.awt.Component
import kotlin.reflect.KMutableProperty0

typealias LatexCompileEditor = CompilerEditor<LatexCompileStep, SupportedLatexCompiler>

/**
 * Collection of fragment builders for the run configuration settings UI.
 * Some of these are based on CommonParameterFragments.
 *
 * @author Sten Wessel
 */
object CommonLatexFragments {

    val standardDimension = JBDimension(300, 30)

    fun createProgramArgumentsFragment(
        id: String,
        message: String,
        commandLinePosition: Int,
        settingsProperty: (RunnerAndConfigurationSettings) -> KMutableProperty0<String?>,
        editorVisible: (RunnerAndConfigurationSettings) -> Boolean = { true },
        name: String? = null,
        group: String? = null
    ): SettingsEditorFragment<LatexRunConfiguration, RawCommandLineEditor> {
        val editor = RawCommandLineEditor().apply {
            minimumSize = JBDimension(400, 30)

            editorField.emptyText.text = message
            editorField.accessibleContext.accessibleName = message
            FragmentedSettingsUtil.setupPlaceholderVisibility(editorField)
            // TODO: make these actually work
            MacrosDialog.addMacroSupport(editorField, MacrosDialog.Filters.ALL) { false }

            setMonospaced(textField)
        }

        // Cannot be SettingsEditorFragment, but has to be RunConfigurationEditorFragment, because in RunConfigurationFragmentedEditor#getRunFragments
        // it filters on RunConfigurationEditorFragments in order to get the fragments in the run config, which are used to create a snapshot in SettingsEditor#getSnapShot()
        // which is used to check if the run configuration was RunConfigurable#isModified() (compared to the saved xml, see BaseRCSettingsConfigurable), so if something is missing in the snapshot
        // it will think it's modified compared to the xml but it isn't.
        // This will lead to LatexCompileSequenceFragment#applyEditorTo to be called continuously, which is very inconvenient when debugging.
        val fragment = object : RunConfigurationEditorFragment<LatexRunConfiguration, RawCommandLineEditor>(
            id, name, group, editor, commandLinePosition, editorVisible
        ) {
            override fun doReset(settings: RunnerAndConfigurationSettingsImpl) {
                (component as? RawCommandLineEditor)?.text = settingsProperty(settings).get()
            }

            override fun applyEditorTo(settings: RunnerAndConfigurationSettingsImpl) {
                settingsProperty(settings).set((component as? RawCommandLineEditor)?.text)
            }
        }

        fragment.isRemovable = true
        fragment.setEditorGetter { e -> e.editorField }

        return fragment
    }

    fun createLatexCompilerFragment(
        commandLinePosition: Int,
        settingsProperty: (LatexRunConfiguration) -> KMutableProperty0<LatexCompiler?>
    ): RunConfigurationEditorFragment<LatexRunConfiguration, LatexCompileEditor> {
        val editor = CompilerEditor("&LaTeX compiler:", CompilerMagic.latexCompilerByExecutableName.values)
        val combobox = editor.component

        setMonospaced(combobox)
        editor.minimumSize = JBDimension(200, 30)
        editor.label.isVisible = false

        combobox.accessibleContext.accessibleName = editor.label.text

        val fragment = object : RunConfigurationEditorFragment<LatexRunConfiguration, LatexCompileEditor>("latexCompiler", "LaTeX compiler", null, editor, commandLinePosition, { true }) {
            override fun doReset(settings: RunnerAndConfigurationSettingsImpl) {
                (component as LatexCompileEditor).setSelectedCompiler(settingsProperty(settings.configuration as LatexRunConfiguration).get())
            }

            override fun applyEditorTo(settings: RunnerAndConfigurationSettingsImpl) {
                settingsProperty(settings.configuration as LatexRunConfiguration).set((component as? LatexCompileEditor)?.getSelectedCompiler() as? LatexCompiler)
            }
        }

        fragment.isRemovable = false
        fragment.setHint("LaTeX compiler or path to executable")

        return fragment
    }

    /**
     * Consider the given path, and replace an absolute part of it by the ProjectFileDirMacro if possible.
     */
    fun insertMacro(path: String, component: Component): String {
        // For better readability for the user, replace absolute path with project path when known
        val baseDir = PlatformDataKeys.PROJECT_FILE_DIRECTORY.getData(DataManager.getInstance().getDataContext(component))
        return if (baseDir != null && baseDir.path in path) {
            path.replace(baseDir.path, "\$${ProjectFileDirMacro().name}\$")
        }
        else {
            path
        }
    }

    fun createMainFileFragment(commandLinePosition: Int, project: Project): RunConfigurationEditorFragment<LatexRunConfiguration, TextFieldWithBrowseButton> {
        val mainFileField = TextFieldWithBrowseButton()
        mainFileField.minimumSize = standardDimension
        mainFileField.addBrowseFolderListener("Choose a File to Compile", "Select the main LaTeX file passed to the compiler", project,
            FileTypeDescriptor("LaTeX File", ".tex"))

        MacrosDialog.addMacroSupport(mainFileField.textField as ExtendableTextField, MacrosDialog.Filters.DIRECTORY_PATH) { false }
        setMonospaced(mainFileField.textField)

        val fragment = object : RunConfigurationEditorFragment<LatexRunConfiguration, TextFieldWithBrowseButton>("mainFile", "Main file", null, mainFileField, commandLinePosition, { true }) {
            override fun doReset(s: RunnerAndConfigurationSettingsImpl) {
                (component as TextFieldWithBrowseButton).text = (s.configuration as LatexRunConfiguration).options.mainFile.pathWithMacro ?: ""
            }

            override fun applyEditorTo(s: RunnerAndConfigurationSettingsImpl) {
                val text = (component as TextFieldWithBrowseButton).text
                val pathWithMacro = insertMacro(text, component)

                val options = (s.configuration as LatexRunConfiguration).options
                options.mainFile = LatexRunConfigurationAbstractPathOption.resolveAndGetPath(pathWithMacro, this.component) { resolved, withMacro -> LatexRunConfigurationPathOption(resolved, withMacro) }
            }
        }

        fragment.isRemovable = false
        return fragment
    }

    // Adapted from CommonParameterFragments#createEnvParameters
    fun createEnvParametersFragment(group: String, commandLinePosition: Int): RunConfigurationEditorFragment<LatexRunConfiguration, EnvironmentVariablesComponent> {
        val env = EnvironmentVariablesComponent()
        env.labelLocation = BorderLayout.WEST
        setMonospaced(env.component.textField)
        env.minimumSize = standardDimension

        val fragment = object : RunConfigurationEditorFragment<LatexRunConfiguration, EnvironmentVariablesComponent>("environmentVariables", "Add environment variables", group, env, commandLinePosition, { s -> (s.configuration as? LatexRunConfiguration)?.envs?.isNotEmpty() == true }) {
            override fun doReset(s: RunnerAndConfigurationSettingsImpl) {
                env.reset(s.configuration as? LatexRunConfiguration)
            }

            override fun applyEditorTo(s: RunnerAndConfigurationSettingsImpl) {
                if (!env.isVisible) {
                    (s.configuration as? LatexRunConfiguration)?.apply {
                        envs = mutableMapOf()
                        isPassParentEnvs = true
                    }
                }
                else {
                    env.apply(s.configuration as? LatexRunConfiguration)
                }
            }
        }

        fragment.isRemovable = true
        fragment.setHint("Separate variables with semicolon: VAR=value; VAR1=value1")
        fragment.actionHint = "Set custom environment variables for compiling LaTeX"
        return fragment
    }

    // Based on CommonParameterFragments
    fun createWorkingDirectoryFragment(group: String, commandLinePosition: Int, project: Project): RunConfigurationEditorFragment<LatexRunConfiguration, LabeledComponent<TextFieldWithBrowseButton>> {
        val workingDirectoryField = TextFieldWithBrowseButton()
        workingDirectoryField.minimumSize = standardDimension
        workingDirectoryField.addBrowseFolderListener("Select Working Directory", null, project, FileChooserDescriptorFactory.createSingleFolderDescriptor(), TextComponentAccessor.TEXT_FIELD_WHOLE_TEXT)
        setMonospaced(workingDirectoryField.textField)

        MacrosDialog.addMacroSupport(workingDirectoryField.textField as ExtendableTextField, MacrosDialog.Filters.DIRECTORY_PATH) { false }

        val field = LabeledComponent.create(workingDirectoryField, "&Working directory:")
        field.labelLocation = BorderLayout.WEST

        val fragment = object : RunConfigurationEditorFragment<LatexRunConfiguration, LabeledComponent<TextFieldWithBrowseButton>>("workingDirectory", "Change default working directory", group, field, commandLinePosition, { s -> (s.configuration as? LatexRunConfiguration)?.hasDefaultWorkingDirectory() == true }) {
            override fun doReset(s: RunnerAndConfigurationSettingsImpl) {
                val runConfig = s.configuration as LatexRunConfiguration
                ((component as LabeledComponent<*>).component as TextFieldWithBrowseButton).text = runConfig.options.workingDirectory.pathWithMacro
                    ?: runConfig.workingDirectory
                    ?: runConfig.options.mainFile.resolve()?.parent?.path
                    ?: ""
            }

            override fun applyEditorTo(s: RunnerAndConfigurationSettingsImpl) {
                val text = ((component as LabeledComponent<*>).component as TextFieldWithBrowseButton).text
                val pathWithMacro = insertMacro(text, component)
                (s.configuration as LatexRunConfiguration).options.workingDirectory = LatexRunConfigurationAbstractPathOption.resolveAndGetPath(pathWithMacro, this.component) { resolved, withMacro -> LatexRunConfigurationPathOption(resolved, withMacro) }
            }
        }

        fragment.isRemovable = true
        return fragment
    }

    /**
     * @param type: Output or auxiliary directory.
     * @param reset: String to use to reset the UI.
     * @param apply: Function to apply the text to the run config.
     * @param isDefault: Whether the value as in the given run config is default.
     */
    fun createOutputPathFragment(group: String, commandLinePosition: Int, project: Project, type: String, reset: (LatexRunConfiguration) -> String, apply: (LatexRunConfiguration, String) -> Unit, isDefault: (LatexRunConfiguration?) -> Boolean?, settings: LatexRunConfiguration): RunConfigurationEditorFragment<LatexRunConfiguration, LabeledComponent<TextFieldWithBrowseButton>> {
        val outputDirectoryField = TextFieldWithBrowseButton()
        outputDirectoryField.minimumSize = standardDimension
        setMonospaced(outputDirectoryField.textField)
        outputDirectoryField.addBrowseFolderListener("Select ${type.capitalize()} Directory", "Select directory to store $type files", project, FileChooserDescriptorFactory.createSingleFolderDescriptor(), TextComponentAccessor.TEXT_FIELD_WHOLE_TEXT)

        MacrosDialog.addMacroSupport(outputDirectoryField.textField as ExtendableTextField, MacrosDialog.Filters.DIRECTORY_PATH) { false }

        val field = LabeledComponent.create(outputDirectoryField, "&${type.capitalize()} directory:")
        field.labelLocation = BorderLayout.WEST

        // Don't show when not applicable (non-MiKTeX)
        val initialVisibility = { s: RunnerAndConfigurationSettingsImpl ->
            val runConfig = s.configuration as? LatexRunConfiguration
            if (type == "auxiliary" && runConfig?.options?.getLatexDistribution(runConfig.project)?.isMiktex() == false) {
                false
            }
            else {
                isDefault((s.configuration as? LatexRunConfiguration)) == false
            }
        }

        val fragment = object : RunConfigurationEditorFragment<LatexRunConfiguration, LabeledComponent<TextFieldWithBrowseButton>>("${type}Directory", "Change default $type directory", group, field, commandLinePosition, initialVisibility) {
            override fun doReset(s: RunnerAndConfigurationSettingsImpl) {
                ((component as LabeledComponent<*>).component as TextFieldWithBrowseButton).text = reset((s.configuration as LatexRunConfiguration))
            }

            override fun applyEditorTo(s: RunnerAndConfigurationSettingsImpl) {
                apply((s.configuration as LatexRunConfiguration), ((component as LabeledComponent<*>).component as TextFieldWithBrowseButton).text)
            }
        }

        fragment.isRemovable = true
        return fragment
    }

    fun createOutputFormatFragment(group: String, commandLinePosition: Int, settings: LatexRunConfiguration): RunConfigurationEditorFragment<LatexRunConfiguration, LabeledComponent<ComboBox<LatexCompiler.OutputFormat>>> {
        val formats = settings.options.compiler?.outputFormats ?: emptyArray()
        val field = LabeledComponent.create(ComboBox(formats), "Output format")
        field.labelLocation = BorderLayout.WEST
        field.size = JBDimension(128, field.height)

        val fragment = object : RunConfigurationEditorFragment<LatexRunConfiguration, LabeledComponent<ComboBox<LatexCompiler.OutputFormat>>>("outputFormat", "Change default output format", group, field, commandLinePosition, { s -> (s.configuration as? LatexRunConfiguration)?.hasDefaultOutputFormat() == false }) {
            override fun doReset(s: RunnerAndConfigurationSettingsImpl) {
                ((component as LabeledComponent<*>).component as ComboBox<*>).selectedItem =
                    (s.configuration as LatexRunConfiguration).options.outputFormat
            }

            override fun applyEditorTo(s: RunnerAndConfigurationSettingsImpl) {
                (s.configuration as LatexRunConfiguration).options.outputFormat =
                    ((component as? LabeledComponent<*>)?.component as? ComboBox<*>)?.selectedItem as? LatexCompiler.OutputFormat
                        ?: LatexCompiler.OutputFormat.PDF
            }
        }

        fragment.isRemovable = true
        return fragment
    }

    fun createLatexDistributionFragment(group: String, commandLinePosition: Int, settings: LatexRunConfiguration): RunConfigurationEditorFragment<LatexRunConfiguration, LabeledComponent<ComboBox<LatexDistributionType>>> {
        val field = LabeledComponent.create(ComboBox(LatexDistributionType.values().filter { it.isAvailable(settings.project) }.toTypedArray()), "LaTeX distribution")
        field.labelLocation = BorderLayout.WEST
        field.size = JBDimension(128, field.height)

        val fragment = object : RunConfigurationEditorFragment<LatexRunConfiguration, LabeledComponent<ComboBox<LatexDistributionType>>>("latexDistribution", "Change default LaTeX distribution", group, field, commandLinePosition, { s -> (s.configuration as? LatexRunConfiguration)?.hasDefaultLatexDistribution() == false }) {
            override fun doReset(s: RunnerAndConfigurationSettingsImpl) {
                ((component as LabeledComponent<*>).component as ComboBox<*>).selectedItem =
                    (s.configuration as LatexRunConfiguration).options.latexDistribution
            }

            override fun applyEditorTo(s: RunnerAndConfigurationSettingsImpl) {
                (s.configuration as LatexRunConfiguration).options.latexDistribution =
                    ((component as? LabeledComponent<*>)?.component as? ComboBox<*>)?.selectedItem as? LatexDistributionType
                        ?: LatexDistributionType.PROJECT_SDK
            }
        }

        fragment.isRemovable = true
        return fragment
    }
}