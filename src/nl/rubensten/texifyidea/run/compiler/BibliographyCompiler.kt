package nl.rubensten.texifyidea.run.compiler

import nl.rubensten.texifyidea.run.BibtexRunConfiguration

/**
 * @author Ruben Schellekens, Sten Wessel
 */
enum class BibliographyCompiler(private val compiler: Compiler<BibtexRunConfiguration>) : Compiler<BibtexRunConfiguration> by compiler {

    BIBTEX(BibtexCompiler);

    override fun toString() = displayName
}