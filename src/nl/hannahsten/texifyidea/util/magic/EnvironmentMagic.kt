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
    val verbatim = EnvironmentNames.run {
        hashSetOf(
            VERBATIM,
            VERBATIM_CAPITAL,
            LST_LISTING,
            PLANTUML,
            MINTED,
            SAGESILENT,
            SAGEBLOCK,
            SAGECOMMANDLINE,
            SAGEVERBATIM,
            SAGEEXAMPLE,
            LUACODE,
            LUACODE_STAR,
            PY_CODE
        )
    }

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
    val languageInjections: Map<String, String> = EnvironmentNames.run {
        mapOf(
            LUACODE to "Lua",
            LUACODE_STAR to "Lua",
            PY_CODE to "Python",
            PY_SUB to "Python",
            PY_VERBATIM to "Python",
            PY_BLOCK to "Python",
            PY_CONSOLE to "Python",
        )
    }

    val algorithmEnvironments = setOf(EnvironmentNames.ALGORITHMIC)
}