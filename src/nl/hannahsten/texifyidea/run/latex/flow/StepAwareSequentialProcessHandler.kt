package nl.hannahsten.texifyidea.run.latex.flow

import com.intellij.execution.KillableProcess
import com.intellij.execution.process.ProcessAdapter
import com.intellij.execution.process.ProcessEvent
import com.intellij.execution.process.ProcessHandler
import com.intellij.execution.process.ProcessOutputTypes
import com.intellij.openapi.util.Key
import nl.hannahsten.texifyidea.TeXception
import java.io.OutputStream
import java.util.concurrent.CopyOnWriteArrayList

/**
 * Executes compile steps sequentially and forwards per-step lifecycle/output events.
 */
internal class StepAwareSequentialProcessHandler(
    val executions: List<BaseLatexStepExecution>,
) : ProcessHandler(), KillableProcess {

    private val listeners = CopyOnWriteArrayList<(StepLogEvent) -> Unit>()
    private val rawLogsByStep = linkedMapOf<Int, StringBuilder>()

    private var currentIndex: Int = -1
    private var currentProcess: ProcessHandler? = null

    private var killed = false
    private var runFinished = false

    init {
        if (executions.isEmpty()) {
            throw TeXception("Cannot create a StepAwareSequentialProcessHandler without steps")
        }
        executions.forEach { execution ->
            rawLogsByStep[execution.index] = StringBuilder()
        }
    }

    fun addStepLogListener(listener: (StepLogEvent) -> Unit) {
        listeners += listener
    }

    fun rawLog(stepIndex: Int): String = rawLogsByStep[stepIndex]?.toString().orEmpty()

    fun rawLogs(): Map<Int, String> = rawLogsByStep.mapValues { (_, value) -> value.toString() }

    override fun startNotify() {
        super.startNotify()
        startStep(0)
    }

    override fun destroyProcessImpl() {
        killed = true
        val process = currentProcess
        if (process != null) {
            process.destroyProcess()
        }
        else {
            finishRun(-1)
        }
    }

    override fun detachProcessImpl() {
        val process = currentProcess
        if (process != null) {
            process.detachProcess()
        }
        else {
            notifyProcessDetached()
        }
    }

    override fun detachIsDefault(): Boolean = false

    override fun getProcessInput(): OutputStream? = null

    override fun canKillProcess(): Boolean = (currentProcess as? KillableProcess)?.canKillProcess() ?: false

    override fun killProcess() {
        killed = true
        val process = currentProcess as? KillableProcess
        if (process != null) {
            process.killProcess()
        }
        else {
            finishRun(-1)
        }
    }

    private fun startStep(index: Int) {
        if (runFinished) {
            return
        }
        if (killed) {
            finishRun(-1)
            return
        }
        if (index !in executions.indices) {
            finishRun(0)
            return
        }

        currentIndex = index
        val execution = executions[index]
        currentProcess = null

        fire(StepLogEvent.StepStarted(execution))
        val preflightError = runCatching { execution.beforeStart() }.exceptionOrNull()
        if (preflightError != null) {
            emitStepOutput(execution, "[TeXiFy] ${preflightError.message ?: "Step preflight failed."}\n", ProcessOutputTypes.STDERR)
            fire(StepLogEvent.StepFinished(execution, 1))
            finishRun(1)
            return
        }

        when (execution) {
            is ProcessLatexStepExecution -> startProcessStep(execution)
            is InlineLatexStepExecution -> executeInlineStep(execution)
        }
    }

    private fun startProcessStep(execution: ProcessLatexStepExecution) {
        val processHandler = execution.processHandler
        currentProcess = processHandler
        processHandler.addProcessListener(object : ProcessAdapter() {
            override fun onTextAvailable(event: ProcessEvent, outputType: Key<*>) {
                if (runFinished) {
                    return
                }
                emitStepOutput(execution, event.text, outputType)
            }

            override fun processTerminated(event: ProcessEvent) {
                if (runFinished) {
                    return
                }
                currentProcess = null
                completeStep(execution, event.exitCode)
            }
        })
        processHandler.startNotify()
    }

    private fun executeInlineStep(execution: InlineLatexStepExecution) {
        val exitCode = runCatching { execution.action() }
            .getOrElse { error ->
                emitStepOutput(execution, "[TeXiFy] ${error.message ?: "Step action failed."}\n", ProcessOutputTypes.STDERR)
                1
            }
        completeStep(execution, exitCode)
    }

    private fun completeStep(execution: BaseLatexStepExecution, exitCode: Int) {
        if (runFinished) {
            return
        }
        runCatching { execution.afterFinish(exitCode) }
            .exceptionOrNull()
            ?.let { error ->
                emitStepOutput(execution, "[TeXiFy] ${error.message ?: "Step post-processing failed."}\n", ProcessOutputTypes.STDERR)
            }
        fire(StepLogEvent.StepFinished(execution, exitCode))
        if (killed || exitCode != 0) {
            finishRun(exitCode)
        }
        else {
            startStep(currentIndex + 1)
        }
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

    private fun emitStepOutput(
        execution: LatexStepExecution,
        text: String,
        outputType: Key<*>,
    ) {
        rawLogsByStep[execution.index]?.append(text)
        notifyTextAvailable(text, outputType)
        fire(StepLogEvent.StepOutput(execution, text, outputType))
    }
}
