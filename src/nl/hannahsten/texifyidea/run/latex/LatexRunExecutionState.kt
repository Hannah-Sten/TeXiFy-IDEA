package nl.hannahsten.texifyidea.run.latex

data class LatexRunExecutionState(
    var isFirstRunConfig: Boolean = true,
    var isLastRunConfig: Boolean = false,
    var hasBeenRun: Boolean = false,
) {

    fun beginAuxChain() {
        isFirstRunConfig = false
    }

    fun markLastPass() {
        isLastRunConfig = true
    }

    fun markIntermediatePass() {
        isLastRunConfig = false
    }

    fun markHasRun() {
        hasBeenRun = true
    }

    fun resetAfterAuxChain() {
        isFirstRunConfig = true
        isLastRunConfig = false
    }
}
