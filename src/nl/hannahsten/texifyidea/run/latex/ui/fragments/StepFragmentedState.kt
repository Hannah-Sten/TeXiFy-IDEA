package nl.hannahsten.texifyidea.run.latex.ui.fragments

import com.intellij.execution.ui.FragmentedSettings
import nl.hannahsten.texifyidea.run.latex.LatexRunConfiguration
import nl.hannahsten.texifyidea.run.latex.LatexStepRunConfigurationOptions

internal class StepFragmentedState : FragmentedSettings {

    lateinit var runConfig: LatexRunConfiguration
    var selectedStepOptions: LatexStepRunConfigurationOptions? = null

    override var selectedOptions: MutableList<FragmentedSettings.Option> = mutableListOf()
}
