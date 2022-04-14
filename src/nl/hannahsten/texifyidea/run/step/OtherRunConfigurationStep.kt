package nl.hannahsten.texifyidea.run.step

import com.intellij.execution.ExecutionTarget
import com.intellij.execution.ExecutionTargetManager
import com.intellij.execution.ExecutionTargetManagerImpl
import com.intellij.execution.RunnerAndConfigurationSettings
import com.intellij.execution.compound.ConfigurationSelectionUtil
import com.intellij.execution.compound.TypeNameTarget
import com.intellij.execution.executors.DefaultRunExecutor
import com.intellij.execution.impl.RunConfigurationBeforeRunProvider
import com.intellij.execution.impl.RunManagerImpl
import com.intellij.execution.process.ProcessHandler
import com.intellij.execution.runners.ExecutionEnvironmentBuilder
import com.intellij.execution.runners.ProgramRunner
import com.intellij.openapi.components.PersistentStateComponent
import nl.hannahsten.texifyidea.run.LatexRunConfiguration
import nl.hannahsten.texifyidea.run.LatexRunConfigurationType
import nl.hannahsten.texifyidea.run.ui.console.LatexExecutionConsole
import java.awt.event.MouseEvent
import java.io.OutputStream

/**
 * Run any other run configuration as a step.
 *
 * Based on [com.intellij.execution.impl.RunConfigurationBeforeRunProvider].
 */
class OtherRunConfigurationStep internal constructor(
    override val provider: StepProvider,
    override var configuration: LatexRunConfiguration
) : Step, PersistentStateComponent<TypeNameTarget> {

    override val name = "Other Run Configuration step"

    private var state = TypeNameTarget()

    // todo use it
    fun getDescription(): String {
        val (settings, target) = mySettingsWithTarget ?: return name
        val text = ConfigurationSelectionUtil.getDisplayText(settings.configuration, target)
        return "Run ''$text''"
    }

    // Cache the settings object
    private var mySettingsWithTarget: Pair<RunnerAndConfigurationSettings, ExecutionTarget>? = null
        get() {
            if (field != null) return field
            val type = this.state.type ?: return null
            val name = this.state.name ?: return null
            val settings = RunManagerImpl.getInstanceImpl(configuration.project).findConfigurationByTypeAndName(type, name) ?: return null
            val targetId = this.state.targetId ?: return null
            val target = (ExecutionTargetManager.getInstance(configuration.project) as ExecutionTargetManagerImpl).findTargetByIdFor(settings.configuration, targetId) ?: return null

            val pair = Pair(settings, target)
            field = pair
            return pair
        }
        set(value) {
            field = value
            val settings = value?.first
            val target = value?.second
            this.state.name = settings?.name
            this.state.type = settings?.type?.id
            this.state.targetId = target?.id
        }

    override fun configure(e: MouseEvent) {
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
        }.show(e.component)
    }

    override fun isValid(): Boolean {
        val configuration = mySettingsWithTarget?.first?.configuration ?: return false
        val executorId = DefaultRunExecutor.getRunExecutorInstance().id
        val runner = ProgramRunner.getRunner(executorId, configuration) ?: return false
        return runner.canRun(executorId, configuration)
    }

    override fun execute(id: String, console: LatexExecutionConsole): ProcessHandler? {
        val (settings, target) = mySettingsWithTarget ?: return null
        val environment = ExecutionEnvironmentBuilder.createOrNull(DefaultRunExecutor.getRunExecutorInstance(), settings)?.build()
            ?: return null

        return object : ProcessHandler() {
            override fun destroyProcessImpl() = notifyProcessTerminated(0)

            override fun detachProcessImpl() = notifyProcessDetached()

            override fun detachIsDefault(): Boolean = false

            override fun getProcessInput(): OutputStream? = null

            override fun startNotify() {
                super.startNotify()
                console.startStep(id, this@OtherRunConfigurationStep, this)
                // todo log output to console
                RunConfigurationBeforeRunProvider.doExecuteTask(environment, settings, target)
                super.notifyProcessTerminated(0) // todo exit code
                console.finishStep(id, 0)
            }
        }
    }

    override fun clone(): Step {
        TODO("Not yet implemented")
    }

    override fun getState(): TypeNameTarget {
        return this.state
    }

    override fun loadState(state: TypeNameTarget) {
        this.state = state
    }
}
