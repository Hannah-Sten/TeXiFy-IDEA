package nl.hannahsten.texifyidea.run.latex.step

import com.intellij.execution.ExecutionException
import com.intellij.execution.KillableProcess
import com.intellij.execution.RunnerAndConfigurationSettings
import com.intellij.execution.process.ProcessAdapter
import com.intellij.execution.process.ProcessEvent
import com.intellij.execution.process.ProcessHandler
import com.intellij.execution.process.ProcessOutputTypes
import com.intellij.execution.process.ProcessTerminatedListener
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.util.Key
import java.io.OutputStream
import java.util.concurrent.CountDownLatch
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger

internal abstract class LegacyAuxRunConfigurationsStep : LatexRunStep {

    abstract fun resolveRunConfigurations(context: LatexRunStepContext): Set<RunnerAndConfigurationSettings>

    override fun createProcess(context: LatexRunStepContext): ProcessHandler {
        val process = object : ProcessHandler(), KillableProcess {

            private val finishing = AtomicBoolean(false)
            private val stopRequested = AtomicBoolean(false)

            @Volatile
            private var currentChild: ProcessHandler? = null

            override fun destroyProcessImpl() {
                stopRequested.set(true)
                currentChild?.destroyProcess() ?: finish(-1)
            }

            override fun detachProcessImpl() {
                stopRequested.set(true)
                currentChild?.detachProcess() ?: notifyProcessDetached()
            }

            override fun detachIsDefault(): Boolean = false

            override fun getProcessInput(): OutputStream? = null

            override fun canKillProcess(): Boolean = (currentChild as? KillableProcess)?.canKillProcess() ?: false

            override fun killProcess() {
                stopRequested.set(true)
                (currentChild as? KillableProcess)?.killProcess() ?: destroyProcessImpl()
            }

            override fun startNotify() {
                super.startNotify()
                val settings = resolveRunConfigurations(context).toList()
                ApplicationManager.getApplication().executeOnPooledThread {
                    if (settings.isEmpty()) {
                        finish(0)
                        return@executeOnPooledThread
                    }

                    for (setting in settings) {
                        if (stopRequested.get()) {
                            finish(-1)
                            return@executeOnPooledThread
                        }

                        val child = try {
                            createChildProcess(context, setting)
                        }
                        catch (e: Exception) {
                            notifyTextAvailable(
                                "Failed to execute `${setting.name}`: ${e.message.orEmpty()}\n",
                                ProcessOutputTypes.STDERR
                            )
                            finish(1)
                            return@executeOnPooledThread
                        }
                        currentChild = child
                        val exitCode = runChildProcess(child)
                        if (exitCode != 0) {
                            finish(exitCode)
                            return@executeOnPooledThread
                        }
                    }
                    finish(0)
                }
            }

            private fun createChildProcess(
                context: LatexRunStepContext,
                settings: RunnerAndConfigurationSettings
            ): ProcessHandler {
                val state = settings.configuration.getState(context.environment.executor, context.environment)
                    ?: throw ExecutionException("No state returned for `${settings.name}`.")
                val result = state.execute(context.environment.executor, context.environment.runner)
                    ?: throw ExecutionException("No execution result returned for `${settings.name}`.")
                return result.processHandler
                    ?: throw ExecutionException("No process handler returned for `${settings.name}`.")
            }

            private fun runChildProcess(child: ProcessHandler): Int {
                val exitCode = AtomicInteger(1)
                val completion = CountDownLatch(1)
                child.addProcessListener(object : ProcessAdapter() {
                    override fun onTextAvailable(event: ProcessEvent, outputType: Key<*>) {
                        notifyTextAvailable(event.text, outputType)
                    }

                    override fun processTerminated(event: ProcessEvent) {
                        exitCode.set(event.exitCode)
                        completion.countDown()
                    }
                })
                child.startNotify()
                try {
                    completion.await()
                }
                catch (_: InterruptedException) {
                    Thread.currentThread().interrupt()
                    return 1
                }
                return exitCode.get()
            }

            private fun finish(exitCode: Int) {
                if (!finishing.compareAndSet(false, true)) {
                    return
                }
                notifyProcessTerminated(exitCode)
            }
        }
        ProcessTerminatedListener.attach(process, context.environment.project)
        return process
    }
}
