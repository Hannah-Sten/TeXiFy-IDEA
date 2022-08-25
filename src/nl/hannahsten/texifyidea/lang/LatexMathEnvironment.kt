package nl.hannahsten.texifyidea.lang

import nl.hannahsten.texifyidea.lang.commands.Argument
import nl.hannahsten.texifyidea.lang.commands.OptionalArgument
import nl.hannahsten.texifyidea.lang.commands.RequiredArgument

/**
 * @author Sten Wessel
 */
enum class LatexMathEnvironment(
    val environmentName: String,
    vararg val arguments: Argument
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
            for (environment in values()) {
                lookup[environment.environmentName] = environment
            }
        }

        @JvmStatic
        operator fun get(name: String) = lookup[name]
    }
}