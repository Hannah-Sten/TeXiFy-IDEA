package nl.hannahsten.texifyidea.run.latex

data class LatexRunExecutionState(
    var isFirstRunConfig: Boolean = true,
    var isLastRunConfig: Boolean = false,
    var hasBeenRun: Boolean = false,
)
