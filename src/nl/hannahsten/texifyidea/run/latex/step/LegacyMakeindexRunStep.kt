package nl.hannahsten.texifyidea.run.latex.step

import com.intellij.execution.RunnerAndConfigurationSettings

internal class LegacyMakeindexRunStep : LegacyAuxRunConfigurationsStep() {

    override val id: String = "legacy-makeindex"

    override fun resolveRunConfigurations(context: LatexRunStepContext): Set<RunnerAndConfigurationSettings> = context.runConfig.makeindexRunConfigs
}
