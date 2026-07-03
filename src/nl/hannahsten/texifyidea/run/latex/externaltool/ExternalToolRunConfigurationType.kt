package nl.hannahsten.texifyidea.run.latex.externaltool

import com.intellij.execution.configurations.ConfigurationFactory
import com.intellij.execution.configurations.ConfigurationType
import nl.hannahsten.texifyidea.TexifyBundle
import nl.hannahsten.texifyidea.TexifyIcons
import nl.hannahsten.texifyidea.run.latex.LatexConfigurationFactory
import javax.swing.Icon

/**
 * Type of [ExternalToolRunConfiguration].
 */
class ExternalToolRunConfigurationType : ConfigurationType {

    override fun getIcon(): Icon = TexifyIcons.BUILD

    override fun getConfigurationTypeDescription() = TexifyBundle.message("runconfig.externaltool.description")

    override fun getId() = "EXTERNAL_TOOL_RUN_CONFIGURATION"

    override fun getDisplayName() = TexifyBundle.message("runconfig.externaltool.displayName")

    override fun getConfigurationFactories(): Array<ConfigurationFactory> = arrayOf(LatexConfigurationFactory(this))
}
