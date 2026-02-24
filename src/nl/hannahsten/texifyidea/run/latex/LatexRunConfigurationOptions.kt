package nl.hannahsten.texifyidea.run.latex

import com.intellij.execution.configurations.LocatableRunConfigurationOptions

class LatexRunConfigurationOptions : LocatableRunConfigurationOptions() {

    internal var model: LatexRunConfigModel = LatexRunConfigModel()
}
