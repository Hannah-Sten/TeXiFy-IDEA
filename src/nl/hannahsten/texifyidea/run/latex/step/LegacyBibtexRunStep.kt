package nl.hannahsten.texifyidea.run.latex.step

import com.intellij.execution.RunnerAndConfigurationSettings
import com.intellij.openapi.application.ReadAction
import com.intellij.openapi.project.DumbService

internal open class LegacyBibtexRunStep : LegacyAuxRunConfigurationsStep() {

    override val id: String = "legacy-bibtex"

    override fun resolveRunConfigurations(context: LatexRunStepContext): Set<RunnerAndConfigurationSettings> {
        val runConfig = context.runConfig
        if (runConfig.bibRunConfigs.isEmpty() && !DumbService.getInstance(runConfig.project).isDumb) {
            ReadAction.run<RuntimeException> {
                runConfig.generateBibRunConfig()
            }
        }
        return runConfig.bibRunConfigs
    }
}
