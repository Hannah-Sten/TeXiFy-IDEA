package nl.hannahsten.texifyidea.run.ui

import com.intellij.execution.RunnerAndConfigurationSettings
import com.intellij.execution.configuration.EnvironmentVariablesComponent
import com.intellij.execution.configurations.RunConfigurationBase
import com.intellij.execution.impl.RunnerAndConfigurationSettingsImpl
import com.intellij.execution.ui.*
import com.intellij.execution.ui.CommonParameterFragments.setMonospaced
import com.intellij.ide.macro.MacrosDialog
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory
import com.intellij.openapi.fileChooser.FileTypeDescriptor
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.LabeledComponent
import com.intellij.openapi.ui.TextComponentAccessor
import com.intellij.openapi.ui.TextFieldWithBrowseButton
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.ui.RawCommandLineEditor
import com.intellij.ui.components.fields.ExtendableTextField
import com.intellij.util.ui.JBDimension
import nl.hannahsten.texifyidea.run.LatexRunConfiguration
import nl.hannahsten.texifyidea.run.compiler.latex.LatexCompiler
import nl.hannahsten.texifyidea.run.compiler.latex.SupportedLatexCompiler
import nl.hannahsten.texifyidea.run.ui.compiler.CompilerEditor
import nl.hannahsten.texifyidea.run.step.LatexCompileStep
import nl.hannahsten.texifyidea.util.magic.CompilerMagic
import org.jetbrains.annotations.NotNull
import java.awt.BorderLayout
import kotlin.reflect.KMutableProperty0

typealias LatexCompileEditor = CompilerEditor<LatexCompileStep, SupportedLatexCompiler>

/**
 * Collection of fragment builders for the run configuration settings UI.
 * Some of these are based on CommonParameterFragments.
 *
 * @author Sten Wessel
 */
object CommonLatexFragments {

    fun programArguments(
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

            CommonParameterFragments.setMonospaced(textField)
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

    fun latexCompiler(
        commandLinePosition: Int,
        settingsProperty: (LatexRunConfiguration) -> KMutableProperty0<LatexCompiler?>
    ): RunConfigurationEditorFragment<LatexRunConfiguration, LatexCompileEditor> {
        val editor = CompilerEditor("&LaTeX compiler:", CompilerMagic.latexCompilerByExecutableName.values)
        val combobox = editor.component

        CommonParameterFragments.setMonospaced(combobox)
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

    fun <S : RunConfigurationBase<*>> file(
        id: String,
        message: String,
        commandLinePosition: Int,
        project: Project,
        settingsProperty: (S) -> KMutableProperty0<VirtualFile?>,
        editorVisible: (RunnerAndConfigurationSettings) -> Boolean = { true },
        name: String? = null,
        group: String? = null
    ): RunConfigurationEditorFragment<S, VirtualFileEditorWithBrowse> {

        val editor = VirtualFileEditorWithBrowse(id, message, project).apply {
            addBrowseFolderListener(
                "Choose a File to Compile",
                "Select the main LaTeX file passed to the compiler",
                FileTypeDescriptor("LaTeX File", ".tex")
            )
        }
        editor.label.isVisible = false
        CommonParameterFragments.setMonospaced(editor.editor)
        editor.minimumSize = JBDimension(300, 30)

        val fragment = object : RunConfigurationEditorFragment<S, VirtualFileEditorWithBrowse>(id, name, group, editor, commandLinePosition, editorVisible) {
            override fun doReset(s: RunnerAndConfigurationSettingsImpl) {
                (component as VirtualFileEditorWithBrowse).selected = settingsProperty(s.configuration as S).get()
            }

            override fun applyEditorTo(s: RunnerAndConfigurationSettingsImpl) {
                settingsProperty(s.configuration as S).set((component as VirtualFileEditorWithBrowse).selected)
            }
        }

        fragment.isRemovable = false
        fragment.setEditorGetter { e -> e.editor }

        return fragment
    }

    // Adapted from CommonParameterFragments#createEnvParameters
    fun createEnvParameters(group: String, commandLinePosition: Int): RunConfigurationEditorFragment<LatexRunConfiguration, EnvironmentVariablesComponent> {
        val env = EnvironmentVariablesComponent()
        env.labelLocation = BorderLayout.WEST
        setMonospaced(env.component.textField)
        val fragment = object : RunConfigurationEditorFragment<LatexRunConfiguration, EnvironmentVariablesComponent>("environmentVariables", "Add environment variables", group, env, commandLinePosition, { s -> (s.configuration as? LatexRunConfiguration)?.environmentVariables?.envs?.isNotEmpty() == true }) {
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
        workingDirectoryField.addBrowseFolderListener("Select Working Directory", null, project, FileChooserDescriptorFactory.createSingleFolderDescriptor(), TextComponentAccessor.TEXT_FIELD_WHOLE_TEXT)
        MacrosDialog.addMacroSupport(workingDirectoryField.textField as @NotNull ExtendableTextField, MacrosDialog.Filters.DIRECTORY_PATH) { false }
        val field = LabeledComponent.create(workingDirectoryField, "&Working directory:")
        field.labelLocation = BorderLayout.WEST
        val fragment = object : RunConfigurationEditorFragment<LatexRunConfiguration, LabeledComponent<TextFieldWithBrowseButton>>("workingDirectory", "Change default working directory", group, field, commandLinePosition, { s -> (s.configuration as? LatexRunConfiguration)?.hasDefaultWorkingDirectory() == true }) {
            override fun doReset(s: RunnerAndConfigurationSettingsImpl) {
                (component as LabeledComponent<TextFieldWithBrowseButton>).component.text = (s.configuration as? LatexRunConfiguration)?.workingDirectory ?: "Something went wrong with resetting this fragment"
            }

            override fun applyEditorTo(s: RunnerAndConfigurationSettingsImpl) {
                (s.configuration as LatexRunConfiguration).workingDirectory = (component as LabeledComponent<TextFieldWithBrowseButton>).text
            }
        }

        fragment.isRemovable = true
        return fragment
    }
}