package nl.hannahsten.texifyidea.run.latexmk

import com.intellij.execution.configurations.ConfigurationFactory
import com.intellij.execution.configurations.ConfigurationType
import com.intellij.execution.configurations.runConfigurationType
import nl.hannahsten.texifyidea.TexifyIcons
import nl.hannahsten.texifyidea.run.latex.LatexConfigurationFactory

internal fun latexmkRunConfigurationType(): LatexmkRunConfigurationType = runConfigurationType<LatexmkRunConfigurationType>()

class LatexmkRunConfigurationType : ConfigurationType {

    override fun getDisplayName() = "Latexmk"

    override fun getConfigurationTypeDescription() = "Build a LaTeX file using latexmk"

    override fun getIcon() = TexifyIcons.BUILD

    override fun getId() = "LATEXMK_RUN_CONFIGURATION"

    override fun getConfigurationFactories(): Array<ConfigurationFactory> = arrayOf(LatexConfigurationFactory(this))
}
