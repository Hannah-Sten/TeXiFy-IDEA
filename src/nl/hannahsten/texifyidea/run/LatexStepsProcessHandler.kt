package nl.hannahsten.texifyidea.run

import com.intellij.build.process.BuildProcessHandler
import com.intellij.execution.process.KillableProcessHandler
import com.intellij.execution.process.ProcessAdapter
import com.intellij.execution.process.ProcessEvent
import java.io.OutputStream

class LatexStepsProcessHandler(private val executionName: String, private val stepProcesses: List<KillableProcessHandler>) : BuildProcessHandler() {

    private var currentProcess: KillableProcessHandler? = null

    init {
        require(stepProcesses.isNotEmpty())

        stepProcesses.dropLast(1).withIndex().forEach { (i, p) ->
            p.addProcessListener(object : ProcessAdapter() {
                override fun processTerminated(event: ProcessEvent) {
                    currentProcess = stepProcesses[i+1]
                    currentProcess?.startNotify()
                }
            })
        }

        stepProcesses.last().addProcessListener(object : ProcessAdapter() {
            override fun processTerminated(event: ProcessEvent) {
                this@LatexStepsProcessHandler.notifyProcessTerminated(event.exitCode)
            }
        })

    }

    override fun startNotify() {
        super.startNotify()
        currentProcess = stepProcesses.firstOrNull()
        currentProcess?.startNotify()
    }

    override fun destroyProcessImpl() {
        currentProcess?.destroyProcess()
    }

    override fun detachProcessImpl() {
        currentProcess?.detachProcess()
    }

    override fun detachIsDefault(): Boolean {
        return false
    }

    override fun getProcessInput(): OutputStream? {
        return null
    }

    override fun getExecutionName() = executionName

}