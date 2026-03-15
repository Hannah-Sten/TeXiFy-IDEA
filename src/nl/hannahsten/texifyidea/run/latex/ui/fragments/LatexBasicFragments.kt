package nl.hannahsten.texifyidea.run.latex.ui.fragments

import com.intellij.execution.configuration.EnvironmentVariablesComponent
import com.intellij.execution.impl.RunnerAndConfigurationSettingsImpl
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
import com.intellij.ui.components.JBCheckBox
import com.intellij.ui.components.fields.ExtendableTextComponent
import com.intellij.ui.components.fields.ExtendableTextField
import nl.hannahsten.texifyidea.index.projectstructure.pathOrNull
import nl.hannahsten.texifyidea.run.latex.LatexDistributionType
import nl.hannahsten.texifyidea.run.latex.LatexPathResolver
import nl.hannahsten.texifyidea.run.latex.LatexRunConfiguration
import nl.hannahsten.texifyidea.run.latex.isInvalidJetBrainsBinPath
import nl.hannahsten.texifyidea.run.latex.ui.LatexDistributionComboBoxRenderer
import nl.hannahsten.texifyidea.run.latex.ui.LatexDistributionSelection
import java.awt.BorderLayout
import java.nio.file.Path
import javax.swing.JComponent
import javax.swing.JPanel

internal object LatexBasicFragments {

    fun createMainFileFragment(
        group: String,
        project: Project
    ): Pair<TextFieldWithBrowseButton, RunConfigurationEditorFragment<LatexRunConfiguration, LabeledComponent<TextFieldWithBrowseButton>>> {
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
        val component = LabeledComponent.create(mainFile, "Main &file")

        val fragment = object : RunConfigurationEditorFragment<LatexRunConfiguration, LabeledComponent<TextFieldWithBrowseButton>>(
            "mainFile",
            "Main file",
            group,
            component,
            0,
            { true }
        ) {
            override fun doReset(s: RunnerAndConfigurationSettingsImpl) {
                val runConfig = s.configuration as LatexRunConfiguration
                mainFile.text = runConfig.mainFilePath.orEmpty()
            }

            override fun applyEditorTo(s: RunnerAndConfigurationSettingsImpl) {
                val runConfig = s.configuration as LatexRunConfiguration
                runConfig.mainFilePath = mainFile.text
            }
        }

        fragment.isRemovable = false
        applyTooltip(component, "Main .tex file")
        return mainFile to fragment
    }

    fun createWorkingDirectoryFragment(group: String, project: Project): RunConfigurationEditorFragment<LatexRunConfiguration, LabeledComponent<JComponent>> = directoryFragment(
        id = "workingDirectory",
        name = "Working directory",
        group = group,
        project = project,
        chooserTitle = "Working Directory",
        labelText = "&Working directory",
        initiallyVisible = { runConfig -> runConfig.workingDirectory != null },
        reset = { runConfig, field ->
            field.text = runConfig.workingDirectory?.toString() ?: LatexPathResolver.MAIN_FILE_PARENT_PLACEHOLDER
        },
        apply = { runConfig, field ->
            runConfig.workingDirectory = field.text
                .takeUnless { it.isBlank() || it == LatexPathResolver.MAIN_FILE_PARENT_PLACEHOLDER }
                ?.let { pathOrNull(it) }
        },
        actionHint = "Set custom working directory",
        tooltip = "Override working directory used by compile/run steps.",
    )

    fun createLatexDistributionFragment(
        group: String,
        project: Project
    ): RunConfigurationEditorFragment<LatexRunConfiguration, LabeledComponent<ComboBox<LatexDistributionSelection>>> {
        val distribution = ComboBox(LatexDistributionSelection.getAvailableSelections(project).toTypedArray()).apply {
            renderer = LatexDistributionComboBoxRenderer(project) { null }
        }
        val component = LabeledComponent.create(distribution, "LaTeX &distribution")

        val fragment = object : RunConfigurationEditorFragment<LatexRunConfiguration, LabeledComponent<ComboBox<LatexDistributionSelection>>>(
            "latexDistribution",
            "LaTeX distribution",
            group,
            component,
            0,
            { s -> (s.configuration as? LatexRunConfiguration)?.latexDistribution != LatexDistributionType.MODULE_SDK }
        ) {
            override fun doReset(s: RunnerAndConfigurationSettingsImpl) {
                val runConfig = s.configuration as LatexRunConfiguration
                refreshDistributionSelections(distribution, project, runConfig.latexDistribution)
                distribution.selectedItem = LatexDistributionSelection.getAvailableSelections(project)
                    .firstOrNull { it.distributionType == runConfig.latexDistribution }
                    ?: LatexDistributionSelection.fromDistributionType(runConfig.latexDistribution)
            }

            override fun applyEditorTo(s: RunnerAndConfigurationSettingsImpl) {
                val runConfig = s.configuration as LatexRunConfiguration
                val selected = distribution.selectedItem as? LatexDistributionSelection
                runConfig.latexDistribution = selected?.distributionType ?: LatexDistributionType.MODULE_SDK
            }
        }

        fragment.isRemovable = true
        fragment.isCanBeHidden = true
        applyTooltip(component, "LaTeX distribution used by compile steps.")
        fragment.actionHint = "Set LaTeX distribution"
        return fragment
    }

    fun createOutputDirectoryFragment(
        group: String,
        project: Project
    ): RunConfigurationEditorFragment<LatexRunConfiguration, LabeledComponent<JComponent>> = directoryFragment(
        id = "outputDirectory",
        name = "Output directory",
        group = group,
        project = project,
        chooserTitle = "Output Files Directory",
        labelText = "&Output directory",
        initiallyVisible = { runConfig -> hasCustomPath(runConfig.outputPath, LatexPathResolver.defaultOutputPath) },
        reset = { runConfig, field ->
            field.text = runConfig.outputPath?.toString() ?: LatexPathResolver.defaultOutputPath.toString()
        },
        apply = { runConfig, field ->
            val text = field.text.trim()
            if (!isInvalidJetBrainsBinPath(text)) {
                runConfig.outputPath = parseDirectoryPath(text, LatexPathResolver.defaultOutputPath)
            }
        },
        actionHint = "Set output directory",
    )

    fun createAuxiliaryDirectoryFragment(
        group: String,
        project: Project
    ): RunConfigurationEditorFragment<LatexRunConfiguration, LabeledComponent<JComponent>> = directoryFragment(
        id = "auxiliaryDirectory",
        name = "Auxiliary directory",
        group = group,
        project = project,
        chooserTitle = "Auxiliary Files Directory",
        labelText = "A&uxiliary directory",
        initiallyVisible = { runConfig -> hasCustomPath(runConfig.auxilPath, LatexPathResolver.defaultAuxilPath) },
        reset = { runConfig, field ->
            field.text = runConfig.auxilPath?.toString() ?: LatexPathResolver.defaultAuxilPath.toString()
        },
        apply = { runConfig, field ->
            runConfig.auxilPath = parseDirectoryPath(field.text.trim(), LatexPathResolver.defaultAuxilPath)
        },
        actionHint = "Set auxiliary directory",
    )

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

    private fun directoryPicker(project: Project, title: String): TextFieldWithBrowseButton = TextFieldWithBrowseButton().apply {
        addBrowseFolderListener(
            TextBrowseFolderListener(
                FileChooserDescriptor(false, true, false, false, false, false)
                    .withTitle(title)
                    .withRoots(*ProjectRootManager.getInstance(project).contentRootsFromAllModules.toSet().toTypedArray())
            )
        )
    }

    private fun directoryFragment(
        id: String,
        name: String,
        group: String,
        project: Project,
        chooserTitle: String,
        labelText: String = name,
        initiallyVisible: (LatexRunConfiguration) -> Boolean,
        reset: (LatexRunConfiguration, TextFieldWithBrowseButton) -> Unit,
        apply: (LatexRunConfiguration, TextFieldWithBrowseButton) -> Unit,
        actionHint: String,
        tooltip: String = "Supports IDE path macros (for example PROJECT_DIR). Legacy placeholders also work.",
    ): RunConfigurationEditorFragment<LatexRunConfiguration, LabeledComponent<JComponent>> {
        val field = directoryPicker(project, chooserTitle)
        val component = LabeledComponent.create(pathFieldWithMacroSupport(field), labelText)

        val fragment = object : RunConfigurationEditorFragment<LatexRunConfiguration, LabeledComponent<JComponent>>(
            id,
            name,
            group,
            component,
            0,
            { s -> initiallyVisible(s.configuration as LatexRunConfiguration) }
        ) {
            override fun doReset(s: RunnerAndConfigurationSettingsImpl) {
                reset(s.configuration as LatexRunConfiguration, field)
            }

            override fun applyEditorTo(s: RunnerAndConfigurationSettingsImpl) {
                apply(s.configuration as LatexRunConfiguration, field)
            }
        }

        fragment.isRemovable = true
        fragment.isCanBeHidden = true
        applyTooltip(component, tooltip)
        fragment.actionHint = actionHint
        return fragment
    }

    private fun pathFieldWithMacroSupport(field: TextFieldWithBrowseButton): JComponent {
        val pathMacros = MacrosDialog.getPathMacros(true)
        (field.textField as? ExtendableTextField)?.let { textField ->
            MacrosDialog.addTextFieldExtension(textField, MacrosDialog.Filters.ANY_PATH, pathMacros)
        }
        return field
    }

    private fun refreshDistributionSelections(
        comboBox: ComboBox<LatexDistributionSelection>,
        project: Project,
        selected: LatexDistributionType
    ) {
        val options = LatexDistributionSelection.getAvailableSelections(project).toMutableList()
        if (options.none { it.distributionType == selected }) {
            options += LatexDistributionSelection.fromDistributionType(selected)
        }
        comboBox.removeAllItems()
        options.forEach(comboBox::addItem)
    }

    private fun parseDirectoryPath(value: String, defaultValue: Path): Path = value
        .takeUnless { it.isBlank() }
        ?.let { pathOrNull(it) }
        ?: defaultValue

    private fun hasCustomPath(path: Path?, defaultValue: Path): Boolean {
        val normalized = path?.toString()?.trim().orEmpty()
        return normalized.isNotBlank() && normalized != defaultValue.toString()
    }

    private fun applyTooltip(component: JComponent, tooltip: String) {
        component.toolTipText = tooltip
        if (component is LabeledComponent<*>) {
            component.component.toolTipText = tooltip
        }
    }
}
