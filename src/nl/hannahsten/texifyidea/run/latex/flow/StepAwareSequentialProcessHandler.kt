package nl.hannahsten.texifyidea.run.latex.flow

import com.intellij.execution.KillableProcess
import com.intellij.execution.process.ProcessAdapter
import com.intellij.execution.process.ProcessEvent
import com.intellij.execution.process.ProcessHandler
import nl.hannahsten.texifyidea.TeXception
import java.io.OutputStream
import java.util.concurrent.CopyOnWriteArrayList

/**
 * Executes compile steps sequentially and forwards per-step lifecycle/output events.
 */
internal class StepAwareSequentialProcessHandler(
    val executions: List<LatexStepExecution>,
) : ProcessHandler(), KillableProcess {

    private val listeners = CopyOnWriteArrayList<(StepLogEvent) -> Unit>()
    private val rawLogsByStep = linkedMapOf<Int, StringBuilder>()

    private var currentExecution: LatexStepExecution? = null
    private var killed = false
    private var runFinished = false

    init {
        if (executions.isEmpty()) {
            throw TeXception("Cannot create a StepAwareSequentialProcessHandler without steps")
        }

        executions.forEach { execution ->
            rawLogsByStep[execution.index] = StringBuilder()
            execution.processHandler.addProcessListener(object : ProcessAdapter() {
                override fun startNotified(event: ProcessEvent) {
                    currentExecution = execution
                    fire(StepLogEvent.StepStarted(execution))
                }

                override fun onTextAvailable(event: ProcessEvent, outputType: com.intellij.openapi.util.Key<*>) {
                    rawLogsByStep[execution.index]?.append(event.text)
                    notifyTextAvailable(event.text, outputType)
                    fire(StepLogEvent.StepOutput(execution, event.text, outputType))
                }

                override fun processTerminated(event: ProcessEvent) {
                    fire(StepLogEvent.StepFinished(execution, event.exitCode))
                }
            })
        }

        executions.dropLast(1).withIndex().forEach { (i, execution) ->
            execution.processHandler.addProcessListener(object : ProcessAdapter() {
                override fun processTerminated(event: ProcessEvent) {
                    if (killed || event.exitCode > 0) {
                        finishRun(event.exitCode)
                    }
                    else {
                        val next = executions[i + 1]
                        currentExecution = next
                        next.processHandler.startNotify()
                    }
                }
            })
        }

        executions.last().processHandler.addProcessListener(object : ProcessAdapter() {
            override fun processTerminated(event: ProcessEvent) {
                finishRun(event.exitCode)
            }
        })
    }

    fun addStepLogListener(listener: (StepLogEvent) -> Unit) {
        listeners += listener
    }

    fun rawLog(stepIndex: Int): String = rawLogsByStep[stepIndex]?.toString().orEmpty()

    fun rawLogs(): Map<Int, String> = rawLogsByStep.mapValues { (_, value) -> value.toString() }

    override fun startNotify() {
        super.startNotify()
        val first = executions.firstOrNull() ?: return
        currentExecution = first
        first.processHandler.startNotify()
    }

    override fun destroyProcessImpl() {
        currentExecution?.processHandler?.destroyProcess()
    }

    override fun detachProcessImpl() {
        currentExecution?.processHandler?.detachProcess()
    }

    override fun detachIsDefault(): Boolean = false

    override fun getProcessInput(): OutputStream? = null

    override fun canKillProcess(): Boolean = (currentExecution?.processHandler as? KillableProcess)?.canKillProcess() ?: false

    override fun killProcess() {
        killed = true
        (currentExecution?.processHandler as? KillableProcess)?.killProcess()
    }

    private fun finishRun(exitCode: Int) {
        if (runFinished) {
            return
        }
        runFinished = true
        fire(StepLogEvent.RunFinished(exitCode))
        notifyProcessTerminated(exitCode)
    }

    private fun fire(event: StepLogEvent) {
        listeners.forEach { listener -> listener(event) }
    }
}
