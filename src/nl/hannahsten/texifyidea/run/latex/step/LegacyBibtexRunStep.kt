package nl.hannahsten.texifyidea.run.latex.step

import com.intellij.execution.RunnerAndConfigurationSettings

internal class LegacyBibtexRunStep : LegacyAuxRunConfigurationsStep() {

    override val id: String = "legacy-bibtex"

    override fun resolveRunConfigurations(context: LatexRunStepContext): Set<RunnerAndConfigurationSettings> = context.runConfig.bibRunConfigs
}
