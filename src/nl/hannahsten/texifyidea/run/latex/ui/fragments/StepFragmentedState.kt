package nl.hannahsten.texifyidea.run.latex.ui.fragments

import com.intellij.execution.ui.FragmentedSettings
import nl.hannahsten.texifyidea.run.latex.LatexRunConfiguration
import nl.hannahsten.texifyidea.run.latex.LatexStepConfig

internal class StepFragmentedState : FragmentedSettings {

    lateinit var runConfig: LatexRunConfiguration
    var selectedStepConfig: LatexStepConfig? = null

    override var selectedOptions: MutableList<FragmentedSettings.Option> = mutableListOf()
}
