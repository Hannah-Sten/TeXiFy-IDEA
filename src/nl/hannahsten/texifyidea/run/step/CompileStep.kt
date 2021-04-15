package nl.hannahsten.texifyidea.run.step

import nl.hannahsten.texifyidea.run.LatexRunConfiguration

/**
 * A step in the compilation process of compiling a LaTeX document.
 * Note that this doesn't have to be a LaTeX compiler, it can be any executable tool.
 *
 * @author Sten Wessel
 */
interface CompileStep {

    val provider: CompileStepProvider

    val configuration: LatexRunConfiguration

    fun configure()

    fun getCommand(): List<String>?

    fun getWorkingDirectory(): String?
}
