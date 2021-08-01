package nl.hannahsten.texifyidea.inspections.grazie

import com.intellij.grazie.text.ProblemFilter
import com.intellij.grazie.text.TextProblem

class LatexProblemFilter : ProblemFilter() {

    override fun shouldIgnore(problem: TextProblem): Boolean {
        return false
    }
}