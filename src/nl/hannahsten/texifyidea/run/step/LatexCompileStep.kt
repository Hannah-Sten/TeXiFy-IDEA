package nl.hannahsten.texifyidea.run.step

import nl.hannahsten.texifyidea.run.latex.LatexRunConfiguration

/**
 * A step in the compilation process of compiling a LaTeX document.
 *
 * @author Sten Wessel
 */
interface LatexCompileStep {

    val provider: LatexCompileStepProvider

    val configuration: LatexRunConfiguration

    fun configure()

    fun getCommand(): List<String>?

    fun getWorkingDirectory(): String?
}
