package nl.hannahsten.texifyidea.util.magic

import com.intellij.openapi.project.Project
import nl.hannahsten.texifyidea.lang.DefaultEnvironment.*
import nl.hannahsten.texifyidea.lang.alias.EnvironmentManager

object EnvironmentMagic {

    val listingEnvironments = hashSetOf(ITEMIZE, ENUMERATE, DESCRIPTION).map { it.env }

    private val tableEnvironmentsWithoutCustomEnvironments = hashSetOf(TABULAR, TABULAR_STAR, TABULARX, TABULARY, ARRAY, LONGTABLE, TABU, MATRIX, BMATRIX, PMATRIX, VMATRIX, VMATRIX_CAPITAL, WIDETABULAR).map { it.env }

    /**
     * Get all table environments in the project, including any user defined aliases.
     */
    fun getAllTableEnvironments(project: Project): Set<String> {
        EnvironmentManager.updateAliases(tableEnvironmentsWithoutCustomEnvironments, project)
        return EnvironmentManager.getAliases(tableEnvironmentsWithoutCustomEnvironments.first())
    }

    /**
     * Environments that define their label via an optional parameter
     */
    val labelAsParameter = hashSetOf(LISTINGS.env, VERBATIM_CAPITAL.env)

    /**
     * Environments that introduce figures
     */
    val figures = hashSetOf(FIGURE.env)

    // Note: used in the lexer
    @JvmField
    val verbatim = hashSetOf(
        VERBATIM.env, VERBATIM_CAPITAL.env, LISTINGS.env, "plantuml", LUACODE.env,
        LUACODE_STAR.env, "sagesilent", "sageblock", "sagecommandline", "sageverbatim", "sageexample", "minted"
    )

    /**
     * Environments that always contain a certain language.
     *
     * Maps the name of the environment to the registered Language id.
     */
    val languageInjections = hashMapOf(
            LUACODE.env to "Lua",
            LUACODE_STAR.env to "Lua"
    )

    val algorithmEnvironments = setOf(ALGORITHMIC.env)

    /**
     * All environments that define a matrix.
     */
    val matrixEnvironments = setOf(
            "matrix", "pmatrix", "bmatrix", "vmatrix", "Bmatrix", "Vmatrix",
            "matrix*", "pmatrix*", "bmatrix*", "vmatrix*", "Bmatrix*", "Vmatrix*",
            "smallmatrix", "psmallmatrix", "bsmallmatrix", "vsmallmatrix", "Bsmallmatrix", "Vsmallmatrix",
            "smallmatrix*", "psmallmatrix*", "bsmallmatrix*", "vsmallmatrix*", "Bsmallmatrix*", "Vsmallmatrix*",
            "gmatrix", "tikz-cd"
    )

    val alignableEnvironments = setOf(
            "eqnarray", "eqnarray*",
            "split",
            "align", "align*",
            "alignat", "alignat*",
            "flalign", "flalign*",
            "aligned", "alignedat",
            "cases", "dcases"
    ) + matrixEnvironments
}