package nl.hannahsten.texifyidea.util.magic

import nl.hannahsten.texifyidea.lang.DefaultEnvironment.*
import nl.hannahsten.texifyidea.lang.Environment

/**
 * Saves typing.
 */
val Environment.env: String
    get() = this.environmentName

object EnvironmentMagic {

    val listingEnvironments = hashSetOf(ITEMIZE, ENUMERATE, DESCRIPTION).map { it.env }

    val tableEnvironments = hashSetOf(TABULAR, TABULAR_STAR, TABULARX, ARRAY, LONGTABLE, TABU).map { it.env }

    /**
     * Map that maps all environments that are expected to have a label to the label prefix they have by convention.
     *
     * environment name `=>` label prefix without colon
     */
    val labeled = mapOf(
        FIGURE.env to "fig",
        TABLE.env to "tab",
        EQUATION.env to "eq",
        ALGORITHM.env to "alg",
        LISTINGS.env to "lst",
        VERBATIM_CAPITAL.env to "verb",
    )

    /**
     * Environments that define their label via an optional parameter
     */
    val labelAsParameter = hashSetOf(LISTINGS.env, VERBATIM_CAPITAL.env)


    /**
     * Environments that introduce figures
     */
    val figures = hashSetOf(FIGURE.env)
}