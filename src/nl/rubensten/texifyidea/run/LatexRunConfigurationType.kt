package nl.rubensten.texifyidea.run

import com.intellij.execution.configurations.ConfigurationFactory
import com.intellij.execution.configurations.ConfigurationType
import com.intellij.execution.configurations.ConfigurationTypeUtil
import nl.rubensten.texifyidea.TexifyIcons

/**
 * @author Ruben Schellekens, Sten Wessel
 */
class LatexRunConfigurationType : ConfigurationType {

    companion object {

        val instance: LatexRunConfigurationType
            get() = ConfigurationTypeUtil.findConfigurationType(LatexRunConfigurationType::class.java)
    }

    override fun getDisplayName() = "LaTeX"

    override fun getConfigurationTypeDescription() = "Build a LaTeX file"

    override fun getIcon() = TexifyIcons.BUILD!!

    override fun getId() = "LATEX_RUN_CONFIGURATION"

    override fun getConfigurationFactories(): Array<ConfigurationFactory> = arrayOf(LatexConfigurationFactory(this))
}
