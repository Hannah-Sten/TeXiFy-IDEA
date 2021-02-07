package nl.hannahsten.texifyidea.run.compiler

import nl.hannahsten.texifyidea.run.bibtex.BibtexRunConfiguration

/**
 * @author Hannah Schellekens, Sten Wessel
 */
enum class BibliographyCompiler(private val compiler: Compiler<BibtexRunConfiguration>) : Compiler<BibtexRunConfiguration> by compiler {

    BIBTEX(BibtexCompiler),
    BIBER(BiberCompiler);

    override fun toString() = displayName

    // TODO: overhaul this as well
    class Converter : com.intellij.util.xmlb.Converter<BibliographyCompiler>() {

        override fun toString(value: BibliographyCompiler) = value.name

        override fun fromString(value: String) = try {
            valueOf(value)
        } catch (_: IllegalArgumentException) { null }
    }

}