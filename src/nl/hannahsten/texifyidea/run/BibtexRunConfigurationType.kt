package nl.hannahsten.texifyidea.run

import com.intellij.execution.configurations.ConfigurationFactory
import com.intellij.execution.configurations.ConfigurationType
import nl.hannahsten.texifyidea.TexifyIcons
import javax.swing.Icon

/**
 * Describes a BibTeX run configuration.
 *
 * @author Sten Wessel
 */
class BibtexRunConfigurationType : ConfigurationType {

    override fun getIcon(): Icon = TexifyIcons.BUILD_BIB

    override fun getConfigurationTypeDescription() = "Build a BibTeX bibliography"

    override fun getId() = "BIBTEX_RUN_CONFIGURATION"

    override fun getDisplayName() = "BibTeX"

    override fun getConfigurationFactories(): Array<ConfigurationFactory> = arrayOf(LatexConfigurationFactory(this))
}
