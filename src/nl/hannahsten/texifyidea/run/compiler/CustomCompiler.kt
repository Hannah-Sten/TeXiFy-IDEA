package nl.hannahsten.texifyidea.run.compiler

import nl.hannahsten.texifyidea.run.executable.CustomExecutable
import nl.hannahsten.texifyidea.run.step.CompileStep

/**
 * A compiler selected by the user, with no built-in support from the plugin.
 *
 * @author Sten Wessel
 */
interface CustomCompiler<in S : CompileStep> : Compiler<S>, CustomExecutable