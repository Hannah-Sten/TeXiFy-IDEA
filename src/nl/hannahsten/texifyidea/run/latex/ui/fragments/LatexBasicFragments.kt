package nl.hannahsten.texifyidea.run.latex.ui.fragments

import com.intellij.execution.configuration.EnvironmentVariablesComponent
import com.intellij.execution.impl.RunnerAndConfigurationSettingsImpl
import com.intellij.execution.ui.CommonParameterFragments
import com.intellij.execution.ui.RunConfigurationEditorFragment
import com.intellij.ide.macro.MacrosDialog
import com.intellij.openapi.fileChooser.FileChooserDescriptor
import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory
import com.intellij.openapi.project.Project
import com.intellij.openapi.roots.ProjectRootManager
import com.intellij.openapi.ui.ComboBox
import com.intellij.openapi.ui.LabeledComponent
import com.intellij.openapi.ui.TextBrowseFolderListener
import com.intellij.openapi.ui.TextFieldWithBrowseButton
import com.intellij.openapi.util.Disposer
import com.intellij.ui.RawCommandLineEditor
import com.intellij.ui.components.JBCheckBox
import com.intellij.ui.components.fields.ExtendableTextComponent
import com.intellij.ui.components.fields.ExtendableTextField
import nl.hannahsten.texifyidea.index.projectstructure.pathOrNull
import nl.hannahsten.texifyidea.run.compiler.LatexCompiler
import nl.hannahsten.texifyidea.run.latex.LatexPathResolver
import nl.hannahsten.texifyidea.run.latex.LatexRunConfiguration
import nl.hannahsten.texifyidea.run.latex.ui.LegacyLatexSettingsEditor
import java.awt.BorderLayout
import javax.swing.JComponent
import javax.swing.JPanel

internal object LatexBasicFragments {

    fun createCompilerFragment(): RunConfigurationEditorFragment<LatexRunConfiguration, LabeledComponent<ComboBox<LatexCompiler>>> {
        val compiler = ComboBox(LatexCompiler.entries.toTypedArray())
        val component = LabeledComponent.create(compiler, "Compiler")

        val fragment = object : RunConfigurationEditorFragment<LatexRunConfiguration, LabeledComponent<ComboBox<LatexCompiler>>>(
            "latexCompiler",
            "Compiler",
            null,
            component,
            10,
            { true }
        ) {
            override fun doReset(s: RunnerAndConfigurationSettingsImpl) {
                val runConfig = s.configuration as LatexRunConfiguration
                component.component.selectedItem = runConfig.compiler ?: LatexCompiler.PDFLATEX
            }

            override fun applyEditorTo(s: RunnerAndConfigurationSettingsImpl) {
                val runConfig = s.configuration as LatexRunConfiguration
                runConfig.compiler = component.component.selectedItem as? LatexCompiler ?: LatexCompiler.PDFLATEX
            }
        }

        fragment.isRemovable = false
        fragment.setHint("LaTeX compiler")
        return fragment
    }

    fun createMainFileFragment(project: Project): RunConfigurationEditorFragment<LatexRunConfiguration, LabeledComponent<TextFieldWithBrowseButton>> {
        val mainFile = TextFieldWithBrowseButton().apply {
            addBrowseFolderListener(
                TextBrowseFolderListener(
                    FileChooserDescriptorFactory.createSingleFileDescriptor()
                        .withTitle("Choose a File to Compile")
                        .withExtensionFilter("tex")
                        .withRoots(*ProjectRootManager.getInstance(project).contentRootsFromAllModules.toSet().toTypedArray())
                )
            )
        }
        val component = LabeledComponent.create(mainFile, "Main file")

        val fragment = object : RunConfigurationEditorFragment<LatexRunConfiguration, LabeledComponent<TextFieldWithBrowseButton>>(
            "mainFile",
            "Main file",
            null,
            component,
            20,
            { true }
        ) {
            override fun doReset(s: RunnerAndConfigurationSettingsImpl) {
                val runConfig = s.configuration as LatexRunConfiguration
                component.component.text = runConfig.mainFilePath.orEmpty()
            }

            override fun applyEditorTo(s: RunnerAndConfigurationSettingsImpl) {
                val runConfig = s.configuration as LatexRunConfiguration
                runConfig.mainFilePath = component.component.text
            }
        }

        fragment.isRemovable = false
        fragment.setHint("Main .tex file")
        return fragment
    }

    fun createCompilerArgumentsFragment(group: String): RunConfigurationEditorFragment<LatexRunConfiguration, RawCommandLineEditor> {
        val editor = RawCommandLineEditor()
        editor.editorField.emptyText.text = "Custom compiler arguments"
        CommonParameterFragments.setMonospaced(editor.textField)

        val fragment = object : RunConfigurationEditorFragment<LatexRunConfiguration, RawCommandLineEditor>(
            "compilerArguments",
            "Compiler arguments",
            group,
            editor,
            30,
            { s -> (s.configuration as? LatexRunConfiguration)?.compilerArguments?.isNotBlank() == true }
        ) {
            override fun doReset(s: RunnerAndConfigurationSettingsImpl) {
                val runConfig = s.configuration as LatexRunConfiguration
                editor.setText(runConfig.compilerArguments.orEmpty())
            }

            override fun applyEditorTo(s: RunnerAndConfigurationSettingsImpl) {
                val runConfig = s.configuration as LatexRunConfiguration
                runConfig.compilerArguments = editor.getText()
            }
        }

        fragment.isRemovable = true
        fragment.isCanBeHidden = true
        fragment.setHint("CLI arguments passed to the selected compiler")
        fragment.actionHint = "Set custom compiler arguments"
        return fragment
    }

    fun createWorkingDirectoryFragment(project: Project): RunConfigurationEditorFragment<LatexRunConfiguration, LabeledComponent<TextFieldWithBrowseButton>> {
        val directoryField = TextFieldWithBrowseButton().apply {
            addBrowseFolderListener(
                TextBrowseFolderListener(
                    FileChooserDescriptor(false, true, false, false, false, false)
                        .withTitle("Working Directory")
                        .withRoots(*ProjectRootManager.getInstance(project).contentRootsFromAllModules)
                )
            )
        }
        val component = LabeledComponent.create(directoryField, "Working directory")

        val fragment = object : RunConfigurationEditorFragment<LatexRunConfiguration, LabeledComponent<TextFieldWithBrowseButton>>(
            "workingDirectory",
            "Working directory",
            null,
            component,
            0,
            { true }
        ) {
            override fun doReset(s: RunnerAndConfigurationSettingsImpl) {
                val runConfig = s.configuration as LatexRunConfiguration
                component.component.text = runConfig.workingDirectory?.toString() ?: LatexPathResolver.MAIN_FILE_PARENT_PLACEHOLDER
            }

            override fun applyEditorTo(s: RunnerAndConfigurationSettingsImpl) {
                val runConfig = s.configuration as LatexRunConfiguration
                runConfig.workingDirectory = component.component.text
                    .takeUnless { it.isBlank() || it == LatexPathResolver.MAIN_FILE_PARENT_PLACEHOLDER }
                    ?.let { pathOrNull(it) }
            }
        }

        fragment.isRemovable = false
        return fragment
    }

    fun createEnvironmentVariablesFragment(group: String): RunConfigurationEditorFragment<LatexRunConfiguration, JComponent> {
        val panel = EnvironmentFragmentPanel()

        val fragment = object : RunConfigurationEditorFragment<LatexRunConfiguration, JComponent>(
            "environmentVariables",
            "Environment variables",
            group,
            panel,
            0,
            { s ->
                (s.configuration as? LatexRunConfiguration)?.let {
                    it.environmentVariables.envs.isNotEmpty() || it.expandMacrosEnvVariables
                } == true
            }
        ) {
            override fun doReset(s: RunnerAndConfigurationSettingsImpl) {
                val runConfig = s.configuration as LatexRunConfiguration
                panel.environmentVariables.envData = runConfig.environmentVariables
                panel.expandMacros.isSelected = runConfig.expandMacrosEnvVariables
            }

            override fun applyEditorTo(s: RunnerAndConfigurationSettingsImpl) {
                val runConfig = s.configuration as LatexRunConfiguration
                runConfig.environmentVariables = panel.environmentVariables.envData
                runConfig.expandMacrosEnvVariables = panel.expandMacros.isSelected
            }
        }

        fragment.isRemovable = true
        fragment.isCanBeHidden = true
        fragment.actionHint = "Set custom environment variables"
        return fragment
    }

    fun createLegacyAdvancedFragment(project: Project): RunConfigurationEditorFragment<LatexRunConfiguration, JComponent> {
        val legacyEditor = LegacyLatexSettingsEditor(project)
        val component = legacyEditor.component

        val fragment = object : RunConfigurationEditorFragment<LatexRunConfiguration, JComponent>(
            "legacyAdvancedOptions",
            "Advanced options (legacy)",
            "Advanced",
            component,
            0,
            { false }
        ) {
            override fun doReset(s: RunnerAndConfigurationSettingsImpl) {
                legacyEditor.resetFrom(s.configuration as LatexRunConfiguration)
            }

            override fun applyEditorTo(s: RunnerAndConfigurationSettingsImpl) {
                if (!isSelected) {
                    return
                }
                legacyEditor.applyTo(s.configuration as LatexRunConfiguration)
            }
        }

        Disposer.register(fragment, legacyEditor)
        fragment.isRemovable = true
        fragment.isCanBeHidden = true
        fragment.actionHint = "Open legacy form for advanced settings not yet migrated to fragments"
        return fragment
    }

    private class EnvironmentFragmentPanel : JPanel(BorderLayout()) {

        val environmentVariables = EnvironmentVariablesComponent()
        val expandMacros = JBCheckBox("Expand macros in environment variables")

        init {
            add(environmentVariables, BorderLayout.CENTER)
            add(expandMacros, BorderLayout.SOUTH)

            val environmentVariableTextField = environmentVariables.component.textField as ExtendableTextField
            var extension: ExtendableTextComponent.Extension? = null

            expandMacros.addItemListener {
                if (it.stateChange == 1) {
                    extension?.let(environmentVariableTextField::addExtension) ?: run {
                        MacrosDialog.addTextFieldExtension(environmentVariableTextField)
                        extension = environmentVariableTextField.extensions.lastOrNull()
                    }
                }
                else {
                    extension?.let(environmentVariableTextField::removeExtension)
                }
            }
        }
    }
}
