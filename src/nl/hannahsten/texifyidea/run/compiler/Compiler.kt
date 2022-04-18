package nl.hannahsten.texifyidea.run.compiler

import nl.hannahsten.texifyidea.run.executable.Executable
import nl.hannahsten.texifyidea.run.step.CompileStep

/**
 * Represents a LaTeX or BibTeX compiler of a step in a LaTeX run configuration.
 *
 * @author Sten Wessel
 */
interface Compiler<in S : CompileStep> : Executable {

    override val displayType: String
        get() = "compiler"

    /**
     * The command to execute to compile [step].
     */
    fun getCommand(step: S): List<String>
}
