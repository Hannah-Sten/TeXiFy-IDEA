package nl.hannahsten.texifyidea.run

import com.intellij.execution.configurations.ConfigurationFactory
import com.intellij.execution.configurations.ConfigurationType
import com.intellij.openapi.project.Project
import nl.hannahsten.texifyidea.run.legacy.bibtex.BibtexRunConfiguration
import nl.hannahsten.texifyidea.run.legacy.bibtex.BibtexRunConfigurationType
import nl.hannahsten.texifyidea.run.legacy.externaltool.ExternalToolRunConfiguration
import nl.hannahsten.texifyidea.run.legacy.externaltool.ExternalToolRunConfigurationType
import nl.hannahsten.texifyidea.run.legacy.makeindex.MakeindexRunConfiguration
import nl.hannahsten.texifyidea.run.legacy.makeindex.MakeindexRunConfigurationType

/**
 * Create template run configurations.
 *
 * @author Sten Wessel
 */
class LatexTemplateConfigurationFactory(type: ConfigurationType) : ConfigurationFactory(type) {

    companion object {

        private const val factoryName = "LaTeX configuration factory"
    }

    override fun createTemplateConfiguration(project: Project) = when (type) {
        is LatexRunConfigurationType -> LatexRunConfiguration(project, this, "LaTeX").apply {
            setDefaultPdfViewer()
            setDefaultOutputFormat()
            setSuggestedName()
            setDefaultDistribution(project)
        }
        is BibtexRunConfigurationType -> BibtexRunConfiguration(project, this, "BibTeX")
        is MakeindexRunConfigurationType -> MakeindexRunConfiguration(project, this, "Makeindex")
        is ExternalToolRunConfigurationType -> ExternalToolRunConfiguration(project, this, "External LaTeX tool")
        else -> throw IllegalArgumentException("No TeXiFy run configuration type, but ${type.id} was received instead.")
    }

    override fun getName() = factoryName

    override fun getId() = factoryName
}