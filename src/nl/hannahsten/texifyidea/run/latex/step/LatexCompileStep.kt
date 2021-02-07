package nl.hannahsten.texifyidea.run.latex.step

import nl.hannahsten.texifyidea.run.latex.LatexRunConfiguration

interface LatexCompileStep {

    val provider: LatexCompileStepProvider

    val configuration: LatexRunConfiguration

    fun configure(): Boolean

    fun execute()
}
