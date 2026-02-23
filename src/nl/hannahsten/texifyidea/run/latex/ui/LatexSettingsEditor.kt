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

class LatexSettingsEditor(settings: LatexRunConfiguration) : RunConfigurationFragmentedEditor<LatexRunConfiguration>(settings) {

    private val latexGroupName = "Compile LaTeX"

    override fun createRunFragments(): MutableList<SettingsEditorFragment<LatexRunConfiguration, *>> {
        val fragments = mutableListOf<SettingsEditorFragment<LatexRunConfiguration, *>>()

        fragments.addAll(BeforeRunFragment.createGroup())
        fragments.add(LatexCompileSequenceFragment(LatexCompileSequenceComponent(this)))
        fragments.add(CommonParameterFragments.createHeader(latexGroupName))
        fragments.add(LatexBasicFragments.createCompilerFragment())
        fragments.add(LatexBasicFragments.createMainFileFragment(project))
        fragments.add(LatexBasicFragments.createCompilerArgumentsFragment(latexGroupName))
        fragments.add(LatexBasicFragments.createWorkingDirectoryFragment(project))
        fragments.add(LatexBasicFragments.createEnvironmentVariablesFragment(latexGroupName))
        fragments.add(LatexBasicFragments.createLegacyAdvancedFragment(project))
        fragments.add(CommonTags.parallelRun())

        return fragments
    }
}
