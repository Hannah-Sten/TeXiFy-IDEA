package nl.hannahsten.texifyidea.run.latex.step

import com.intellij.execution.RunnerAndConfigurationSettings

internal class LegacyExternalToolRunStep : LegacyAuxRunConfigurationsStep() {

    override val id: String = "legacy-external-tool"

    override fun resolveRunConfigurations(context: LatexRunStepContext): Set<RunnerAndConfigurationSettings> = context.runConfig.externalToolRunConfigs
}
