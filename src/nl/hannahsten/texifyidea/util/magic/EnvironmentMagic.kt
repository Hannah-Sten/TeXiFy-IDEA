package nl.hannahsten.texifyidea.util.magic

import nl.hannahsten.texifyidea.lang.predefined.EnvironmentNames
import nl.hannahsten.texifyidea.lang.predefined.EnvironmentNames.FIGURE

object EnvironmentMagic {

    val listingEnvironments: Set<String> = setOf(
        EnvironmentNames.ITEMIZE,
        EnvironmentNames.ENUMERATE,
        EnvironmentNames.DESCRIPTION
    )

    /**
     * Environments that define their label via an optional parameter
     */
    val labelAsParameter = hashSetOf(EnvironmentNames.LST_LISTING, EnvironmentNames.VERBATIM_CAPITAL)

    /**
     * Environments that introduce figures
     */
    val figures = hashSetOf(FIGURE)

    // Note: used in the lexer
    @JvmField
    val verbatim = hashSetOf(
        EnvironmentNames.VERBATIM,
        EnvironmentNames.VERBATIM_CAPITAL,
        EnvironmentNames.LST_LISTING,
        EnvironmentNames.PLANTUML,
        EnvironmentNames.MINTED,
        EnvironmentNames.SAGESILENT,
        EnvironmentNames.SAGEBLOCK,
        EnvironmentNames.SAGECOMMANDLINE,
        EnvironmentNames.SAGEVERBATIM,
        EnvironmentNames.SAGEEXAMPLE,
        EnvironmentNames.LUACODE,
        EnvironmentNames.LUACODE_STAR,
        EnvironmentNames.PY_CODE
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
        EnvironmentNames.LUACODE to "Lua",
        EnvironmentNames.LUACODE_STAR to "Lua",
        EnvironmentNames.PY_CODE to "python",
        EnvironmentNames.PY_SUB to "python",
        EnvironmentNames.PY_VERBATIM to "python",
        EnvironmentNames.PY_BLOCK to "python",
        EnvironmentNames.PY_CONSOLE to "python",
    )

    val algorithmEnvironments = setOf(EnvironmentNames.ALGORITHMIC)
}