package nl.rubensten.texifyidea.run.compiler

import nl.rubensten.texifyidea.run.LatexRunConfiguration

/**
 * @author Ruben Schellekens, Sten Wessel
 */
enum class DocumentCompiler(private val compiler: Compiler<LatexRunConfiguration>) : Compiler<LatexRunConfiguration> by compiler {

    PDFLATEX_MIKTEX(PdflatexMiktexCompiler),
    PDFLATEX_OTHER(PdflatexOtherCompiler);

    override fun toString() = displayName

    /**
     * @author Ruben Schellekens
     */
    enum class Format {
        PDF, DVI;

        companion object {
            @JvmStatic
            fun byNameIgnoreCase(name: String?): Format? {
                for (format in values()) {
                    if (format.name.equals(name!!, ignoreCase = true)) {
                        return format
                    }
                }

                return null
            }
        }
    }
}
