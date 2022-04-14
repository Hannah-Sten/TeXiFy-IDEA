package nl.hannahsten.texifyidea.run.step

import com.intellij.icons.AllIcons
import nl.hannahsten.texifyidea.run.LatexRunConfiguration
import javax.swing.Icon

object OtherRunConfigurationStepProvider : StepProvider {

    override val name: String = "Run another configuration"

    override val icon: Icon = AllIcons.Actions.Execute // todo also use task icon somewhere

    override val id: String = "other-run-configuration"

    override fun createStep(configuration: LatexRunConfiguration) = OtherRunConfigurationStep(this, configuration)

    override fun createIfRequired(runConfiguration: LatexRunConfiguration): List<Step> {
        return listOf(createStep(runConfiguration))
    }
}