package nl.hannahsten.texifyidea.run.compiler.latex

import nl.hannahsten.texifyidea.run.compiler.Compiler
import nl.hannahsten.texifyidea.run.step.LatexCompileStep

abstract class LatexCompiler : Compiler<LatexCompileStep> {

    /**
     * Whether the compiler supports input files with Unicode encoding.
     */
    open val supportsUnicode = false

    /**
     * Whether the compiler includes running bibtex/biber.
     */
    open val includesBibtex = false

    /**
     * Whether the compiler includes running index programs.
     */
    open val includesMakeindex = false

    /**
     * Whether the compiler automatically determines the number of compiles needed.
     */
    open val handlesNumberOfCompiles = false

    /**
     * List of output formats supported by this compiler.
     */
    open val outputFormats: Array<OutputFormat> = arrayOf(OutputFormat.PDF, OutputFormat.DVI)

    class Converter : com.intellij.util.xmlb.Converter<LatexCompiler>() {

        override fun toString(value: LatexCompiler) = when (value) {
            is SupportedLatexCompiler -> value.executableName
            is CustomLatexCompiler -> value.executablePath
            else -> ""
        }

        override fun fromString(value: String): LatexCompiler {
            return SupportedLatexCompiler.byExecutableName(value) ?: CustomLatexCompiler(value)
        }
    }

    /**
     * @author Hannah Schellekens
     */
    enum class OutputFormat {

        DEFAULT, // Means: don't overwite the default, e.g. a default from the latexmkrc, i.e. don't add any command line parameters
        PDF,
        DVI,
        HTML,
        XDV,
        AUX;

        companion object {

            fun byNameIgnoreCase(name: String?): OutputFormat {
                return values().firstOrNull {
                    it.name.equals(name, ignoreCase = true)
                } ?: PDF
            }
        }
    }
}
