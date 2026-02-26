package nl.hannahsten.texifyidea.run.latex.flow

import com.intellij.execution.KillableProcess
import com.intellij.execution.process.ProcessAdapter
import com.intellij.execution.process.ProcessEvent
import com.intellij.execution.process.ProcessHandler
import com.intellij.execution.process.ProcessOutputTypes
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.util.Key
import nl.hannahsten.texifyidea.TeXception
import nl.hannahsten.texifyidea.run.latex.step.InlineLatexRunStep
import nl.hannahsten.texifyidea.run.latex.step.LatexRunStep
import nl.hannahsten.texifyidea.run.latex.step.LatexRunStepContext
import nl.hannahsten.texifyidea.run.latex.step.ProcessLatexRunStep
import java.io.OutputStream
import java.util.concurrent.CopyOnWriteArrayList

/**
 * Executes compile steps sequentially and forwards per-step lifecycle/output events.
 */
internal class StepAwareSequentialProcessHandler(
    val steps: List<LatexRunStep>,
    private val context: LatexRunStepContext,
) : ProcessHandler(), KillableProcess {

    private val listeners = CopyOnWriteArrayList<(StepLogEvent) -> Unit>()
    private val rawLogsByStep = linkedMapOf<Int, StringBuilder>()

    private var currentIndex: Int = -1
    private var currentProcess: ProcessHandler? = null

    private var killed = false
    private var runFinished = false

    init {
        if (steps.isEmpty()) {
            throw TeXception("Cannot create a StepAwareSequentialProcessHandler without steps")
        }
        steps.indices.forEach { index ->
            rawLogsByStep[index] = StringBuilder()
        }
    }

    fun addStepLogListener(listener: (StepLogEvent) -> Unit) {
        listeners += listener
    }

    fun rawLog(stepIndex: Int): String = rawLogsByStep[stepIndex]?.toString().orEmpty()

    override fun startNotify() {
        super.startNotify()
        startFirstStep()
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

    private fun startFirstStep() {
        val application = ApplicationManager.getApplication()
        if (application.isDispatchThread) {
            application.executeOnPooledThread {
                safeStartStep(0)
            }
            return
        }
        safeStartStep(0)
    }

    private fun safeStartStep(index: Int) {
        try {
            startStep(index)
        }
        catch (error: Exception) {
            failRunForException(index, steps.getOrNull(index), error)
        }
    }

    private fun failRunForException(
        stepIndex: Int?,
        step: LatexRunStep?,
        error: Exception,
    ) {
        if (runFinished) {
            return
        }

        val summary = error.message?.takeIf { it.isNotBlank() }
            ?: "Step start failed (${error::class.java.simpleName})."
        val stackTrace = error.stackTraceToString().trimEnd()
        val text = buildString {
            append("[TeXiFy] ")
            append(summary)
            append('\n')
            append(stackTrace)
            append('\n')
        }

        if (step != null && stepIndex != null && stepIndex in steps.indices) {
            emitStepOutput(stepIndex, step, text, ProcessOutputTypes.STDERR)
            fire(StepLogEvent.StepFinished(stepIndex, step, 1))
        }
        else {
            notifyTextAvailable(text, ProcessOutputTypes.STDERR)
        }

        finishRun(1)
    }

    private fun startStep(index: Int) {
        if (runFinished) {
            return
        }
        if (killed) {
            finishRun(-1)
            return
        }
        if (index !in steps.indices) {
            finishRun(0)
            return
        }

        currentIndex = index
        val step = steps[index]
        currentProcess = null

        fire(StepLogEvent.StepStarted(index, step))
        step.beforeStart(context)

        when (step) {
            is ProcessLatexRunStep -> startProcessStep(index, step)
            is InlineLatexRunStep -> executeInlineStep(index, step)
            else -> {
                emitStepOutput(index, step, "[TeXiFy] Unsupported step implementation: ${step::class.simpleName}\n", ProcessOutputTypes.STDERR)
                fire(StepLogEvent.StepFinished(index, step, 1))
                finishRun(1)
            }
        }
    }

    private fun startProcessStep(index: Int, step: ProcessLatexRunStep) {
        val processHandler = step.createProcess(context)

        currentProcess = processHandler
        processHandler.addProcessListener(object : ProcessAdapter() {
            override fun onTextAvailable(event: ProcessEvent, outputType: Key<*>) {
                if (runFinished) {
                    return
                }
                emitStepOutput(index, step, event.text, outputType)
            }

            override fun processTerminated(event: ProcessEvent) {
                if (runFinished) {
                    return
                }
                currentProcess = null
                completeStep(index, step, event.exitCode)
            }
        })
        processHandler.startNotify()
    }

    private fun executeInlineStep(index: Int, step: InlineLatexRunStep) {
        val exitCode = step.runInline(context)
        completeStep(index, step, exitCode)
    }

    private fun completeStep(index: Int, step: LatexRunStep, exitCode: Int) {
        if (runFinished) {
            return
        }
        try {
            step.afterFinish(context, exitCode)
        }
        catch (error: Exception) {
            failRunForException(index, step, error)
        }
        fire(StepLogEvent.StepFinished(index, step, exitCode))
        if (killed || exitCode != 0) {
            finishRun(exitCode)
        }
        else {
            safeStartStep(currentIndex + 1)
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
        index: Int,
        step: LatexRunStep,
        text: String,
        outputType: Key<*>,
    ) {
        rawLogsByStep[index]?.append(text)
        notifyTextAvailable(text, outputType)
        fire(StepLogEvent.StepOutput(index, step, text, outputType))
    }
}
