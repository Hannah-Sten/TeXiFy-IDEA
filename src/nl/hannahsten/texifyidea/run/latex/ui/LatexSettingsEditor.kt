package nl.hannahsten.texifyidea.run.latex.ui

import com.intellij.execution.impl.RunnerAndConfigurationSettingsImpl
import com.intellij.execution.ui.BeforeRunFragment
import com.intellij.execution.ui.BeforeRunComponent
import com.intellij.execution.ui.CommonParameterFragments
import com.intellij.execution.ui.CommonTags
import com.intellij.execution.ui.RunConfigurationFragmentedEditor
import com.intellij.execution.ui.SettingsEditorFragment
import com.intellij.openapi.ui.TextFieldWithBrowseButton
import nl.hannahsten.texifyidea.run.latex.LatexRunConfiguration
import nl.hannahsten.texifyidea.run.latex.LatexStepRunConfigurationOptions
import nl.hannahsten.texifyidea.run.latex.LatexRunConfigurationStaticSupport
import nl.hannahsten.texifyidea.run.latex.step.LatexStepAutoConfigurator
import nl.hannahsten.texifyidea.run.latex.ui.fragments.LatexBasicFragments
import nl.hannahsten.texifyidea.run.latex.ui.fragments.LatexCompileSequenceComponent
import nl.hannahsten.texifyidea.run.latex.ui.fragments.LatexCompileSequenceFragment
import nl.hannahsten.texifyidea.run.latex.ui.fragments.LatexStepSelectionState
import nl.hannahsten.texifyidea.run.latex.ui.fragments.LatexStepSettingsComponent
import nl.hannahsten.texifyidea.run.latex.ui.fragments.LatexStepSettingsFragment
import nl.hannahsten.texifyidea.util.files.psiFile

/**
 * Fragmented editor for [LatexRunConfiguration] and the coordination point for step editing.
 *
 * This editor brings together two different views of the compile pipeline:
 * [LatexCompileSequenceFragment] edits the sequence itself, while [LatexStepSettingsFragment] edits the settings of the
 * currently selected step. The editor keeps those views working against the same in-progress configuration so they stay
 * synchronized during a single settings session.
 *
 * JetBrains treats fragmented run configuration editors as continuously snapshotted state. In practice that means:
 * - [resetEditorFrom] must stay a pure UI reset with no write-back side effects.
 * - [SettingsEditorFragment.fireEditorStateChanged] should only represent real user edits.
 * - [applyEditorTo] may be called during dirty-state polling, not only on the final Apply/OK action.
 *
 * References:
 * https://plugins.jetbrains.com/docs/intellij/run-configurations.html
 * https://intellij-support.jetbrains.com/hc/en-us/community/posts/4459894734610-Run-configuration-is-never-saved
 */
class LatexSettingsEditor(
    runConfiguration: LatexRunConfiguration,
) : RunConfigurationFragmentedEditor<LatexRunConfiguration>(runConfiguration) {

    private val commonGroupName = "Common settings"
    internal val shadowSteps = mutableListOf<LatexStepRunConfigurationOptions>()
    internal val compileSequenceComponent = LatexCompileSequenceComponent(this, project)
    internal val stepSettingsComponent = LatexStepSettingsComponent(project, this)

    private val runConfig: LatexRunConfiguration
        get() = mySettings

    private lateinit var mainFileTextField: TextFieldWithBrowseButton

    override fun createRunFragments(): MutableList<SettingsEditorFragment<LatexRunConfiguration, *>> {
        val fragments = mutableListOf<SettingsEditorFragment<LatexRunConfiguration, *>>()

        fragments.add(CommonParameterFragments.createHeader(commonGroupName))
        val (mainFileTextField, mainFileFrag) = LatexBasicFragments.createMainFileFragment(commonGroupName, project)
        this.mainFileTextField = mainFileTextField
        fragments.add(mainFileFrag)
        fragments.add(LatexBasicFragments.createLatexDistributionFragment(commonGroupName, project))
        fragments.add(LatexBasicFragments.createWorkingDirectoryFragment(commonGroupName, project))
        fragments.add(LatexBasicFragments.createOutputDirectoryFragment(commonGroupName, project))
        fragments.add(LatexBasicFragments.createAuxiliaryDirectoryFragment(commonGroupName, project))
        fragments.add(LatexBasicFragments.createEnvironmentVariablesFragment(commonGroupName))
        fragments.add(LatexCompileSequenceFragment(compileSequenceComponent))
        fragments.add(LatexStepSettingsFragment(stepSettingsComponent, ::onStepSettingsChanged))

        // platform fragments
        fragments.add(BeforeRunFragment.createBeforeRun(BeforeRunComponent(this), null))
        fragments.addAll(BeforeRunFragment.createGroup())
        fragments.add(CommonTags.parallelRun())

        return fragments
    }

    internal var configFromReset: LatexRunConfiguration? = null
        private set

    internal fun configForUiContext(): LatexRunConfiguration = configFromReset ?: runConfig

    override fun resetEditorFrom(s: RunnerAndConfigurationSettingsImpl) {
        (s.configuration as? LatexRunConfiguration)?.let {
            // we have to first update the steps before resetting the fragments
            configFromReset = it
            shadowSteps.clear()
            shadowSteps.addAll(it.copyStepsForUi())
        }
        super.resetEditorFrom(s)
    }

    override fun applyEditorTo(settings: LatexRunConfiguration) {
        super.applyEditorTo(settings)
        val stepsToSave = shadowSteps
            .map { it.deepCopy() }
            .toMutableList()
        stepSettingsComponent.applyCurrentSelectionTo(stepsToSave)
        settings.replaceStepsFromUi(stepsToSave)

        // JetBrains support notes that applyEditorTo() is also used to build periodic dirty-state snapshots.
        // During those calls the platform applies the editor into a temporary snapshot, not the live configuration.
        // Only mirror the saved batch-apply result back into the shared UI state when the platform is applying to the
        // live run configuration instance that backs this editor session.
        if (settings === configForUiContext()) {
            stepSettingsComponent.applyCurrentSelectionTo(shadowSteps)
            stepSettingsComponent.onStepsChanged()
            compileSequenceComponent.refreshStepTitles()
        }
    }

    internal fun beforeSequenceStructureChange() {
        stepSettingsComponent.flushCurrentStep()
    }

    internal fun onCompileSequenceSelectionChanged(selectionState: LatexStepSelectionState) {
        stepSettingsComponent.onStepSelectionChanged(selectionState)
    }

    internal fun onCompileSequenceStepsChanged() {
        stepSettingsComponent.onStepsChanged()
    }

    internal fun onStepSettingsChanged() {
        stepSettingsComponent.flushCurrentStep()
        compileSequenceComponent.refreshStepTitles()
    }

    internal fun autoConfigureCurrentSteps(): List<LatexStepRunConfigurationOptions> {
        val mainFilePath = if (::mainFileTextField.isInitialized) mainFileTextField.text else runConfig.mainFilePath
        val mainFile = LatexRunConfigurationStaticSupport.resolveMainFile(runConfig, mainFilePath)
        val configuredSteps = LatexStepAutoConfigurator.completeSteps(mainFile?.psiFile(project), shadowSteps)
        shadowSteps.clear()
        shadowSteps.addAll(configuredSteps)
        return shadowSteps
    }
}
