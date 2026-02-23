package nl.hannahsten.texifyidea.run.latex.step

import com.intellij.execution.RunnerAndConfigurationSettings
import com.intellij.execution.impl.RunConfigurationBeforeRunProvider
import com.intellij.execution.process.ProcessHandler
import com.intellij.execution.process.ProcessTerminatedListener
import java.io.OutputStream

internal abstract class LegacyAuxRunConfigurationsStep : LatexRunStep {

    abstract fun resolveRunConfigurations(context: LatexRunStepContext): Set<RunnerAndConfigurationSettings>

    override fun createProcess(context: LatexRunStepContext): ProcessHandler {
        val process = object : ProcessHandler() {
            override fun destroyProcessImpl() {
                notifyProcessTerminated(0)
            }

            override fun detachProcessImpl() {
                notifyProcessDetached()
            }

            override fun detachIsDefault(): Boolean = false

            override fun getProcessInput(): OutputStream? = null

            override fun startNotify() {
                super.startNotify()
                var exitCode = 0
                for (settings in resolveRunConfigurations(context)) {
                    val ok = RunConfigurationBeforeRunProvider.doExecuteTask(context.environment, settings, null)
                    if (!ok) {
                        exitCode = 1
                        break
                    }
                }
                notifyProcessTerminated(exitCode)
            }
        }
        ProcessTerminatedListener.attach(process, context.environment.project)
        return process
    }
}
