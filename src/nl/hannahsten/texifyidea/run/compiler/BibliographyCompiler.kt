package nl.hannahsten.texifyidea.run.compiler

import nl.hannahsten.texifyidea.run.bibtex.BibtexRunConfiguration

/**
 * @author Hannah Schellekens, Sten Wessel
 */
enum class BibliographyCompiler(private val compiler: Compiler<BibtexRunConfiguration>) : Compiler<BibtexRunConfiguration> by compiler {

    BIBTEX(BibtexCompiler),
    BIBER(BiberCompiler);

    override fun toString() = displayName
}