package nl.hannahsten.texifyidea.run.latex.ui

import com.intellij.execution.impl.RunnerAndConfigurationSettingsImpl
import com.intellij.execution.ui.BeforeRunFragment
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
import nl.hannahsten.texifyidea.run.latex.ui.fragments.LatexStepSettingsComponent
import nl.hannahsten.texifyidea.run.latex.ui.fragments.LatexStepSettingsFragment
import nl.hannahsten.texifyidea.util.files.psiFile

/**
 * Fragmented run-configuration editor for [LatexRunConfiguration].
 *
 * Step-related UI is split into three layers:
 *
 * 1. Editor-owned `shadowSteps`
 *    This editor owns the single mutable draft step list used while the run configuration UI is open. It is created as
 *    a deep copy of `configuration.configOptions.steps` during reset and is the only step state shared by the step
 *    sequence UI and the step settings UI.
 *
 * 2. [LatexCompileSequenceComponent] and [LatexCompileSequenceFragment]
 *    The sequence component is a structural view over `shadowSteps`. It is responsible for add/remove, reorder, type
 *    changes, and auto-configure, but it no longer owns a separate persisted step list.
 *
 * 3. [LatexStepSettingsComponent] and [LatexStepSettingsFragment]
 *    These render the type-specific editor cards for the currently selected draft step in `shadowSteps`. The component
 *    contains one [com.intellij.openapi.options.SettingsEditor] per supported step kind and flushes field edits back
 *    into the currently selected draft step before selection or structure changes.
 *
 * 4. [LatexSettingsEditor.applyEditorTo]
 *    This is the only place that writes steps back to [LatexRunConfiguration]. Fragments only flush local UI state;
 *    after that this editor copies the final `shadowSteps` list into the run configuration.
 *
 * Current data flow:
 *
 * - The editor owns and resets `shadowSteps`.
 * - [LatexCompileSequenceComponent] mutates `shadowSteps` for structure changes.
 * - [LatexStepSettingsComponent] mutates the selected step inside `shadowSteps` for field changes.
 * - Structural mutations call into step settings first so the active card flushes its pending edits before drag/add/
 *   remove/type-change operations replace or reorder steps.
 * - The two step-related fragments are added in order:
 *   [LatexCompileSequenceFragment], then [LatexStepSettingsFragment].
 *   This means IntelliJ reset/apply also visits them in that order.
 *
 * Current lifecycle implications:
 *
 * - On reset, `shadowSteps` is populated once and both step UIs bind to that shared list.
 * - On apply, fragments flush local state into `shadowSteps`, then this editor writes `shadowSteps` back to the run
 *   configuration in one place.
 *
 * This class intentionally keeps the wiring centralized so future changes to step-state ownership or apply ordering
 * can be reasoned about from a single entry point.
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
        fragments.add(LatexStepSettingsFragment(stepSettingsComponent))
        fragments.addAll(BeforeRunFragment.createGroup())
        fragments.add(CommonTags.parallelRun())

        return fragments
    }

    internal var configFromReset: LatexRunConfiguration? = null
        private set

    /*
    Note: there are two `resetEditorFrom`/`applyEditorTo` pairs in this editor:
    resetEditorFrom(s: RunnerAndConfigurationSettingsImpl) goes first and is called by the framework, and sub-fragments are reset
    resetEditorFrom(settings: LatexRunConfiguration) is then called
     */
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
        settings.replaceStepsFromUi(shadowSteps)
    }

    internal fun beforeSequenceStructureChange() {
        stepSettingsComponent.flushCurrentStep()
    }

    internal fun onCompileSequenceSelectionChanged(index: Int, stepId: String?, type: String?) {
        stepSettingsComponent.onStepSelectionChanged(index, stepId, type)
    }

    internal fun onCompileSequenceStepsChanged() {
        stepSettingsComponent.onStepsChanged()
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
