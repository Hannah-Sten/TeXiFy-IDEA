package nl.hannahsten.texifyidea.run.latex.ui.fragments

import com.intellij.execution.ui.FragmentedSettings
import nl.hannahsten.texifyidea.run.latex.LatexRunConfiguration

internal class StepFragmentedState : FragmentedSettings {

    lateinit var runConfig: LatexRunConfiguration

    override var selectedOptions: MutableList<FragmentedSettings.Option> = mutableListOf()
}
