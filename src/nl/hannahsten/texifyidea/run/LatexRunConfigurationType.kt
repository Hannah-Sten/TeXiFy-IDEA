package nl.hannahsten.texifyidea.run

import com.intellij.execution.configurations.ConfigurationFactory
import com.intellij.execution.configurations.ConfigurationType
import com.intellij.execution.configurations.runConfigurationType
import nl.hannahsten.texifyidea.TexifyIcons

internal fun latexRunConfigurationType(): LatexRunConfigurationType = runConfigurationType<LatexRunConfigurationType>()

/**
 * Registers the LaTeX run configuration as a run configuration.
 *
 * @author Hannah Schellekens, Sten Wessel
 */
class LatexRunConfigurationType : ConfigurationType {

    override fun getDisplayName() = "LaTeX"

    override fun getConfigurationTypeDescription() = "Build a LaTeX file"

    override fun getIcon() = TexifyIcons.BUILD

    override fun getId() = "LATEX_RUN_CONFIGURATION"

    override fun getConfigurationFactories(): Array<ConfigurationFactory> = arrayOf(LatexTemplateConfigurationFactory(this))
}
