package nl.hannahsten.texifyidea.run.latex

import com.intellij.execution.configurations.ConfigurationFactory
import com.intellij.execution.configurations.ConfigurationType
import com.intellij.openapi.project.Project
import nl.hannahsten.texifyidea.run.bibtex.BibtexRunConfiguration
import nl.hannahsten.texifyidea.run.bibtex.BibtexRunConfigurationType
import nl.hannahsten.texifyidea.run.latex.externaltool.ExternalToolRunConfiguration
import nl.hannahsten.texifyidea.run.latex.externaltool.ExternalToolRunConfigurationType
import nl.hannahsten.texifyidea.run.compiler.LatexCompiler
import nl.hannahsten.texifyidea.run.compiler.LatexCompiler.Format
import nl.hannahsten.texifyidea.run.latexmk.LatexmkRunConfiguration
import nl.hannahsten.texifyidea.run.latexmk.LatexmkRunConfigurationType
import nl.hannahsten.texifyidea.run.makeindex.MakeindexRunConfiguration
import nl.hannahsten.texifyidea.run.makeindex.MakeindexRunConfigurationType

/**
 * @author Sten Wessel
 */
class LatexConfigurationFactory(type: ConfigurationType) : ConfigurationFactory(type) {

    companion object {

        private const val FACTORY_NAME = "LaTeX configuration factory"
    }

    override fun createTemplateConfiguration(project: Project) = when (type) {
        is LatexRunConfigurationType -> LatexRunConfiguration(project, this, "LaTeX").apply {
            setDefaultCompiler()
            setDefaultPdfViewer()
            setDefaultOutputFormat()
            setSuggestedName()
            setDefaultLatexDistribution()
        }
        is LatexmkRunConfigurationType -> LatexmkRunConfiguration(project, this, "Latexmk").apply {
            compiler = LatexCompiler.LATEXMK
            setDefaultPdfViewer()
            outputFormat = Format.DEFAULT
            setSuggestedName()
            setDefaultLatexDistribution()
        }
        is BibtexRunConfigurationType -> BibtexRunConfiguration(project, this, "BibTeX")
        is MakeindexRunConfigurationType -> MakeindexRunConfiguration(project, this, "Makeindex")
        is ExternalToolRunConfigurationType -> ExternalToolRunConfiguration(project, this, "External LaTeX tool")
        else -> throw IllegalArgumentException("No TeXiFy run configuration type, but ${type.id} was received instead.")
    }

    override fun getName() = FACTORY_NAME

    override fun getId() = FACTORY_NAME
}
