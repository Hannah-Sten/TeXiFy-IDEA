package nl.hannahsten.texifyidea.run.compiler

import nl.hannahsten.texifyidea.run.step.CompileStep

/**
 * A compiler that has built-in support from the plugin.
 *
 * @author Sten Wessel
 */
interface SupportedCompiler<in S : CompileStep> : Compiler<S> {

    val displayName: String
    val executableName: String
}