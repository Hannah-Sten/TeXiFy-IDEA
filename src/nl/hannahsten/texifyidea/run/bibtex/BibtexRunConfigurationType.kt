package nl.hannahsten.texifyidea.run.bibtex

import com.intellij.execution.configurations.ConfigurationFactory
import com.intellij.execution.configurations.ConfigurationType
import nl.hannahsten.texifyidea.TexifyBundle
import nl.hannahsten.texifyidea.TexifyIcons
import nl.hannahsten.texifyidea.run.latex.LatexConfigurationFactory
import javax.swing.Icon

/**
 * Describes a BibTeX run configuration.
 *
 * @author Sten Wessel
 */
class BibtexRunConfigurationType : ConfigurationType {

    override fun getIcon(): Icon = TexifyIcons.BUILD_BIB

    override fun getConfigurationTypeDescription() = TexifyBundle.message("runconfig.bibtex.description")

    override fun getId() = "BIBTEX_RUN_CONFIGURATION"

    override fun getDisplayName() = TexifyBundle.message("runconfig.bibtex.displayName")

    override fun getConfigurationFactories(): Array<ConfigurationFactory> = arrayOf(LatexConfigurationFactory(this))
}
