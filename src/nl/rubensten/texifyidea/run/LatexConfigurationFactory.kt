package nl.rubensten.texifyidea.run

import com.intellij.execution.configurations.ConfigurationFactory
import com.intellij.execution.configurations.ConfigurationType
import com.intellij.execution.configurations.RunConfiguration
import com.intellij.openapi.project.Project

/**
 * @author Sten Wessel
 */
class LatexConfigurationFactory(type: ConfigurationType) : ConfigurationFactory(type) {

    companion object {
        private val FACTORY_NAME = "LaTeX configuration factory"
    }

    override fun createTemplateConfiguration(project: Project): RunConfiguration {
        return when (type) {
            is LatexRunConfigurationType -> LatexRunConfiguration(project, this, "LaTeX")
            is BibtexRunConfigurationType -> BibtexRunConfiguration(project, this, "BibTeX")
            else -> throw IllegalArgumentException("No TeXiFy run configuration type, but ${type.id} was received instead.")
        }
    }

    override fun getName(): String {
        return FACTORY_NAME
    }

}
