package nl.hannahsten.texifyidea.run.latex

import com.intellij.execution.process.KillableProcessHandler
import com.intellij.execution.runners.ExecutionEnvironment

interface LatexCompilationPipeline {

    fun prepare(runConfig: LatexCompilationRunConfiguration, environment: ExecutionEnvironment)

    fun buildCommand(runConfig: LatexCompilationRunConfiguration, environment: ExecutionEnvironment): List<String>

    fun scheduleAuxRuns(runConfig: LatexCompilationRunConfiguration, handler: KillableProcessHandler, environment: ExecutionEnvironment)

    fun scheduleViewer(runConfig: LatexCompilationRunConfiguration, handler: KillableProcessHandler, environment: ExecutionEnvironment)

    fun scheduleCleanup(runConfig: LatexCompilationRunConfiguration, handler: KillableProcessHandler)
}
