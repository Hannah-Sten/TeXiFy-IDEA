package nl.hannahsten.texifyidea.run.step

import com.intellij.execution.ExecutionTarget
import com.intellij.execution.ExecutionTargetManager
import com.intellij.execution.ExecutionTargetManagerImpl
import com.intellij.execution.RunnerAndConfigurationSettings
import com.intellij.execution.compound.ConfigurationSelectionUtil
import com.intellij.execution.compound.TypeNameTarget
import com.intellij.execution.impl.RunManagerImpl
import com.intellij.openapi.components.PersistentStateComponent
import nl.hannahsten.texifyidea.run.LatexRunConfiguration
import nl.hannahsten.texifyidea.run.LatexRunConfigurationType

class OtherRunConfigurationStep internal constructor(
    override val provider: StepProvider,
    override var configuration: LatexRunConfiguration
) : CompileStep(), PersistentStateComponent<TypeNameTarget> {

    override val name = "Other Run Configuration step"

    // Cache the settings object
    private var mySettingsWithTarget: Pair<RunnerAndConfigurationSettings, ExecutionTarget>? = null
        get() {
            if (field != null) return field
            val type = this.state?.type ?: return null
            val name = this.state?.name ?: return null
            val settings = RunManagerImpl.getInstanceImpl(configuration.project).findConfigurationByTypeAndName(type, name) ?: return null
            val targetId = this.state?.targetId ?: return null
            val target = (ExecutionTargetManager.getInstance(configuration.project) as ExecutionTargetManagerImpl).findTargetByIdFor(settings.configuration, targetId) ?: return null

            val pair = Pair(settings, target)
            field = pair
            return pair
        }
        set(value) {
            field = value
            val settings = value?.first
            val target = value?.second
            this.state?.name = settings.name
            this.state?.type = settings.type.id
            this.state?.targetId = target?.id
        }

    override fun configure() {
        // See RunConfigurationBeforeRunProvider#configureTask
        val project = configuration.project
        val runManager = RunManagerImpl.getInstanceImpl(project)
        val configurations = RunManagerImpl.getInstanceImpl(project).allSettings
            .filter { it.type is LatexRunConfigurationType }
            .map { it.configuration }
        ConfigurationSelectionUtil.createPopup(project, runManager, configurations) { selectedConfigs, selectedTarget ->
            val selectedSettings = selectedConfigs
                .firstOrNull()
                ?.let { runManager.getSettings(it) } ?: return@createPopup

            mySettingsWithTarget = Pair(selectedSettings, selectedTarget)
        }.showInBestPositionFor(context)
    }
}