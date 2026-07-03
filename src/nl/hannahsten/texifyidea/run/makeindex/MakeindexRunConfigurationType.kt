package nl.hannahsten.texifyidea.run.makeindex

import com.intellij.execution.configurations.ConfigurationFactory
import com.intellij.execution.configurations.ConfigurationType
import nl.hannahsten.texifyidea.TexifyBundle
import nl.hannahsten.texifyidea.TexifyIcons
import nl.hannahsten.texifyidea.run.latex.LatexConfigurationFactory
import javax.swing.Icon

/**
 * Type of [MakeindexRunConfiguration].
 */
class MakeindexRunConfigurationType : ConfigurationType {

    override fun getIcon(): Icon = TexifyIcons.BUILD

    override fun getConfigurationTypeDescription() = TexifyBundle.message("runconfig.makeindex.description")

    override fun getId() = "MAKEINDEX_RUN_CONFIGURATION"

    override fun getDisplayName() = TexifyBundle.message("runconfig.makeindex.displayName")

    override fun getConfigurationFactories(): Array<ConfigurationFactory> = arrayOf(LatexConfigurationFactory(this))
}
