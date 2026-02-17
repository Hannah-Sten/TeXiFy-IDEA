package nl.hannahsten.texifyidea.run.latex

import com.intellij.execution.RunnerAndConfigurationSettings

/**
 * Extension contract for chained LaTeX compilation workflows (latex -> bib/makeindex -> latex).
 */
interface LatexChainedCompilationRunConfiguration : LatexCompilationRunConfiguration {

    var compileTwice: Boolean
    var isLastRunConfig: Boolean
    var isFirstRunConfig: Boolean

    var bibRunConfigs: Set<RunnerAndConfigurationSettings>
    var makeindexRunConfigs: Set<RunnerAndConfigurationSettings>
    var externalToolRunConfigs: Set<RunnerAndConfigurationSettings>

    fun getAllAuxiliaryRunConfigs(): Set<RunnerAndConfigurationSettings>

    fun generateBibRunConfig()
}
