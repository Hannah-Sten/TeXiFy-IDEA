package nl.hannahsten.texifyidea.run.compiler

import nl.hannahsten.texifyidea.run.step.LatexCompileStep

/**
 * A compiler selected by the user, with no built-in support from the plugin.
 *
 * @author Sten Wessel
 */
interface CustomCompiler<in S : LatexCompileStep> : Compiler<S> {

    val executablePath: String
}