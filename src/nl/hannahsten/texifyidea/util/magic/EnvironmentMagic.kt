package nl.hannahsten.texifyidea.util.magic

import com.intellij.openapi.project.Project
import nl.hannahsten.texifyidea.lang.DefaultEnvironment.*
import nl.hannahsten.texifyidea.lang.alias.EnvironmentManager

object EnvironmentMagic {

    val listingEnvironments: Set<String> = listOf(ITEMIZE, ENUMERATE, DESCRIPTION).map { it.env }.toSet()

    private val tableEnvironmentsWithoutCustomEnvironments: Set<String> =
        hashSetOf(TABULAR, TABULAR_STAR, TABULARX, TABULARY, ARRAY, LONGTABLE, TABU, MATRIX, MATRIX_STAR, BMATRIX, BMATRIX_STAR, PMATRIX,
            PMATRIX_STAR, VMATRIX, VMATRIX_STAR, VMATRIX_CAPITAL, VMATRIX_CAPITAL_STAR, WIDETABULAR, BLOCKARRAY, BLOCK, TBLR, LONGTBLR, TALLTBLR).map { it.env }
            .toSet()

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
        VERBATIM.env, VERBATIM_CAPITAL.env, LISTINGS.env, "plantuml", LUACODE.env, LUACODE_STAR.env, PYCODE.env,
        "sagesilent", "sageblock", "sagecommandline", "sageverbatim", "sageexample", "minted"
    )

    /**
     * Do a guess whether the environment is a verbatim environment.
     * Note: used in the lexer, so it should be fast.
     */
    @JvmStatic
    fun isProbablyVerbatim(environmentName: String): Boolean {
        // It might use \newminted environments, which always end in code
        // There are other environments that have 'code' in their name, if they are all verbatim environments is unclear
        // See https://github.com/Hannah-Sten/TeXiFy-IDEA/issues/2847#issuecomment-1347941386
        return verbatim.contains(environmentName) || environmentName.endsWith("code")
    }

    /**
     * Environments that always contain a certain language.
     *
     * Maps the name of the environment to the registered Language id.
     */
    val languageInjections = hashMapOf(
        LUACODE.env to "Lua",
        LUACODE_STAR.env to "Lua",
        PYCODE.env to "python",
        PYSUB.env to "python",
        PYVERBATIM.env to "python",
        PYBLOCK.env to "python",
        PYCONSOLE.env to "python",
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