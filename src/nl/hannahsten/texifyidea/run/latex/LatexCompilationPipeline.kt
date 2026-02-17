package nl.hannahsten.texifyidea.run.latex

import com.intellij.execution.process.KillableProcessHandler
import com.intellij.execution.runners.ExecutionEnvironment

interface LatexCompilationPipeline {

    fun prepare(runConfig: LatexCompilationRunConfiguration, environment: ExecutionEnvironment, context: LatexExecutionContext)

    fun buildCommand(runConfig: LatexCompilationRunConfiguration, environment: ExecutionEnvironment, context: LatexExecutionContext): List<String>

    fun finalize(runConfig: LatexCompilationRunConfiguration, handler: KillableProcessHandler, environment: ExecutionEnvironment, context: LatexExecutionContext)
}
