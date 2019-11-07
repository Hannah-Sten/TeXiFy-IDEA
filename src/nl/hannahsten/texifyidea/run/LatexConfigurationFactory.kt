package nl.hannahsten.texifyidea.run

import com.intellij.execution.configurations.ConfigurationFactory
import com.intellij.execution.configurations.ConfigurationType
import com.intellij.openapi.project.Project

/**
 * @author Sten Wessel
 */
class LatexConfigurationFactory(type: ConfigurationType) : ConfigurationFactory(type) {

    companion object {
        private const val factoryName = "LaTeX configuration factory"
    }

    override fun createTemplateConfiguration(project: Project) = when (type) {
        is LatexRunConfigurationType -> LatexRunConfiguration(project, this, "LaTeX")
        is BibtexRunConfigurationType -> BibtexRunConfiguration(project, this, "BibTeX")
        else -> throw IllegalArgumentException("No TeXiFy run configuration type, but ${type.id} was received instead.")
    }

    override fun getName() = factoryName
}