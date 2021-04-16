package nl.hannahsten.texifyidea.run.ui

import com.intellij.execution.RunnerAndConfigurationSettings
import com.intellij.execution.configurations.RunConfigurationBase
import com.intellij.execution.impl.RunnerAndConfigurationSettingsImpl
import com.intellij.execution.ui.CommonParameterFragments
import com.intellij.execution.ui.FragmentedSettingsUtil
import com.intellij.execution.ui.RunConfigurationEditorFragment
import com.intellij.execution.ui.SettingsEditorFragment
import com.intellij.ide.macro.MacrosDialog
import com.intellij.openapi.fileChooser.FileTypeDescriptor
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.ui.RawCommandLineEditor
import com.intellij.util.ui.JBDimension
import nl.hannahsten.texifyidea.run.LatexRunConfiguration
import nl.hannahsten.texifyidea.run.compiler.latex.LatexCompiler
import nl.hannahsten.texifyidea.run.compiler.latex.SupportedLatexCompiler
import nl.hannahsten.texifyidea.run.ui.compiler.CompilerEditor
import nl.hannahsten.texifyidea.run.step.LatexCompileStep
import nl.hannahsten.texifyidea.util.magic.CompilerMagic
import kotlin.reflect.KMutableProperty0

typealias LatexCompileEditor = CompilerEditor<LatexCompileStep, SupportedLatexCompiler>

/**
 * Collection of fragment builders for the run configuration settings UI.
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

        // Cannot be SettingsEditorFragment, but has to be RunConfigurationEditorFragment, bcause in RunConfigurationFragmentedEditor#getRunFragments
        // it filters on RunConfigurationEditorFragments in order to get the fragments in the run config, which are used to create a snapshot in SettingsEditor#getSnapShot()
        // which is used to check if the run configuration was modified (compared to the saved xml), so if something is missing in the snapshot
        // it will think it's modified compared to the xml but it isn't.
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

        fragment.isRemovable = false
        fragment.setEditorGetter { e -> e.editorField }

        return fragment
    }

    fun latexCompiler(
        commandLinePosition: Int,
        settingsProperty: (LatexRunConfiguration) -> KMutableProperty0<LatexCompiler?>
    ): SettingsEditorFragment<LatexRunConfiguration, LatexCompileEditor> {
        val editor = CompilerEditor("&LaTeX compiler:", CompilerMagic.latexCompilerByExecutableName.values)
        val combobox = editor.component

        CommonParameterFragments.setMonospaced(combobox)
        editor.minimumSize = JBDimension(200, 30)
        editor.label.isVisible = false

        combobox.accessibleContext.accessibleName = editor.label.text

        val fragment = SettingsEditorFragment<LatexRunConfiguration, LatexCompileEditor>(
            "latexCompiler", "&LaTeX compiler", null, editor, commandLinePosition,
            { settings, component -> component.setSelectedCompiler(settingsProperty(settings).get()) },
            { settings, component -> settingsProperty(settings).set(component.getSelectedCompiler() as LatexCompiler) },
            { true }
        )

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
        editorVisible: (S) -> Boolean = { true },
        name: String? = null,
        group: String? = null
    ): SettingsEditorFragment<S, VirtualFileEditorWithBrowse> {

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

        val fragment = SettingsEditorFragment(
            id, name, group, editor, commandLinePosition,
            { settings, component -> component.selected = settingsProperty(settings).get() },
            { settings, component -> settingsProperty(settings).set(component.selected) },
            editorVisible
        )

        fragment.isRemovable = false
        fragment.setEditorGetter { e -> e.editor }

        return fragment
    }
}