package nl.hannahsten.texifyidea.run.latex.ui.fragments

internal data class LatexStepSelectionState(
    val selectedStepIds: List<String> = emptyList(),
    val primaryStepId: String? = null,
) {

    companion object {

        val EMPTY = LatexStepSelectionState()
    }

    val isEmpty: Boolean
        get() = selectedStepIds.isEmpty()
}
