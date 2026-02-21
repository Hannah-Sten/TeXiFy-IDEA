package nl.hannahsten.texifyidea.run.latex

data class LatexRunExecutionState(
    var isFirstRunConfig: Boolean = true,
    var isLastRunConfig: Boolean = false,
    var hasBeenRun: Boolean = false,
) {
    fun syncTo(runConfig: LatexRunConfiguration) {
        runConfig.isFirstRunConfig = isFirstRunConfig
        runConfig.isLastRunConfig = isLastRunConfig
        runConfig.hasBeenRun = hasBeenRun
    }

    companion object {
        fun from(runConfig: LatexRunConfiguration): LatexRunExecutionState = LatexRunExecutionState(
            isFirstRunConfig = runConfig.isFirstRunConfig,
            isLastRunConfig = runConfig.isLastRunConfig,
            hasBeenRun = runConfig.hasBeenRun,
        )
    }
}
