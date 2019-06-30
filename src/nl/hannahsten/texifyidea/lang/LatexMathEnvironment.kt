package nl.hannahsten.texifyidea.lang

import java.util.*

/**
 * @author Sten Wessel
 */
enum class LatexMathEnvironment(
        val environmentName: String,
        vararg val arguments: Argument,
        val initialContents: String? = null
) {

    /*
     *  Default LaTeX.
     */
    ARRAY("array", OptionalArgument("pos"), RequiredArgument("cols")),
    BMATRIX("bmatrix"),
    CAPITAL_BMATRIX("Bmatrix"),
    MATRIX("matrix"),
    PMATRIX("pmatrix"),
    VMATRIX("vmatrix"),
    CAPITAL_VMATRIX("Vmatrix");

    companion object {

        private val lookup = HashMap<String, LatexMathEnvironment>()

        init {
            for (environment in LatexMathEnvironment.values()) {
                lookup[environment.environmentName] = environment
            }
        }

        @JvmStatic
        operator fun get(name: String) = lookup[name]
    }
}