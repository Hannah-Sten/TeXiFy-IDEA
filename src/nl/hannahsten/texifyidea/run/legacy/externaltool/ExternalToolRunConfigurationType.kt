package nl.hannahsten.texifyidea.run.legacy.externaltool

import com.intellij.execution.configurations.ConfigurationFactory
import com.intellij.execution.configurations.ConfigurationType
import nl.hannahsten.texifyidea.TexifyIcons
import nl.hannahsten.texifyidea.run.LatexTemplateConfigurationFactory
import javax.swing.Icon

/**
 * Type of [ExternalToolRunConfiguration].
 */
class ExternalToolRunConfigurationType : ConfigurationType {

    override fun getIcon(): Icon = TexifyIcons.BUILD

    override fun getConfigurationTypeDescription() = "External LaTeX tool run configuration"

    override fun getId() = "EXTERNAL_TOOL_RUN_CONFIGURATION"

    override fun getDisplayName() = "External LaTeX Tool"

    override fun getConfigurationFactories(): Array<ConfigurationFactory> = arrayOf(LatexTemplateConfigurationFactory(this))
}
