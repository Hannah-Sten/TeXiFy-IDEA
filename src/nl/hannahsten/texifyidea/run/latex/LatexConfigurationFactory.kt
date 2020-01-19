package nl.hannahsten.texifyidea.run.latex

import com.intellij.execution.configurations.ConfigurationFactory
import com.intellij.execution.configurations.ConfigurationType
import com.intellij.openapi.project.Project
import nl.hannahsten.texifyidea.run.bibtex.BibtexRunConfiguration
import nl.hannahsten.texifyidea.run.bibtex.BibtexRunConfigurationType
import nl.hannahsten.texifyidea.run.makeindex.MakeindexRunConfiguration
import nl.hannahsten.texifyidea.run.makeindex.MakeindexRunConfigurationType

/**
 * @author Sten Wessel
 */
class LatexConfigurationFactory(type: ConfigurationType) : ConfigurationFactory(type) {

    companion object {
        private const val factoryName = "LaTeX configuration factory"
    }

    override fun createTemplateConfiguration(project: Project) = when (type) {
        is LatexRunConfigurationType -> LatexRunConfiguration(project, this, "LaTeX").apply {
            setDefaultCompiler()
            setDefaultAuxiliaryDirectories()
            setDefaultOutputFormat()
            setSuggestedName()
        }
        is BibtexRunConfigurationType -> BibtexRunConfiguration(project, this, "BibTeX")
        is MakeindexRunConfigurationType -> MakeindexRunConfiguration(project, this, "Makeindex")
        else -> throw IllegalArgumentException("No TeXiFy run configuration type, but ${type.id} was received instead.")
    }

    override fun getName() = factoryName
}