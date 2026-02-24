package nl.hannahsten.texifyidea.run.latex.ui

import com.intellij.execution.ui.BeforeRunFragment
import com.intellij.execution.ui.CommonParameterFragments
import com.intellij.execution.ui.CommonTags
import com.intellij.execution.ui.RunConfigurationFragmentedEditor
import com.intellij.execution.ui.SettingsEditorFragment
import nl.hannahsten.texifyidea.run.latex.LatexRunConfiguration
import nl.hannahsten.texifyidea.run.latex.ui.fragments.LatexBasicFragments
import nl.hannahsten.texifyidea.run.latex.ui.fragments.LatexCompileSequenceComponent
import nl.hannahsten.texifyidea.run.latex.ui.fragments.LatexCompileSequenceFragment
import nl.hannahsten.texifyidea.run.latex.ui.fragments.LatexStepSettingsComponent
import nl.hannahsten.texifyidea.run.latex.ui.fragments.LatexStepSettingsFragment

class LatexSettingsEditor(settings: LatexRunConfiguration) : RunConfigurationFragmentedEditor<LatexRunConfiguration>(settings) {

    private val commonGroupName = "Common settings"

    override fun createRunFragments(): MutableList<SettingsEditorFragment<LatexRunConfiguration, *>> {
        val fragments = mutableListOf<SettingsEditorFragment<LatexRunConfiguration, *>>()
        val compileSequenceComponent = LatexCompileSequenceComponent(this)
        val stepSettingsComponent = LatexStepSettingsComponent(this, project)
        compileSequenceComponent.onSelectionChanged = { index, stepId, type ->
            stepSettingsComponent.onStepSelectionChanged(index, stepId, type)
        }
        compileSequenceComponent.onStepsChanged = { steps ->
            stepSettingsComponent.onStepsChanged(steps)
        }

        fragments.add(CommonParameterFragments.createHeader(commonGroupName))
        fragments.add(LatexBasicFragments.createMainFileFragment(commonGroupName, project))
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
}
