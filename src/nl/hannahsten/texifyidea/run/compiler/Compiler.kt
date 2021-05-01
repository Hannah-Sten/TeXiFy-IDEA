package nl.hannahsten.texifyidea.run.compiler

import nl.hannahsten.texifyidea.run.step.CompileStep

/**
 * Represents a LaTeX or BibTeX compiler of a step in a LaTeX run configuration.
 *
 * @author Sten Wessel
 */
interface Compiler<in S : CompileStep> {

    /**
     * The command to execute to compile [step].
     *
     * Returns `null` if no command should be executed.
     */
    fun getCommand(step: S): List<String>?
}

