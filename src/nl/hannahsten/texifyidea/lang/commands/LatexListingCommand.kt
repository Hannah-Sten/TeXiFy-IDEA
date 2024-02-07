package nl.hannahsten.texifyidea.lang.commands

import nl.hannahsten.texifyidea.lang.LatexPackage
import nl.hannahsten.texifyidea.lang.LatexPackage.Companion.LISTINGS
import nl.hannahsten.texifyidea.lang.LatexPackage.Companion.LUACODE
import nl.hannahsten.texifyidea.lang.LatexPackage.Companion.PYTHONTEX

/**
 * @author Hannah Schellekens
 */
enum class LatexListingCommand(
    override val command: String,
    override vararg val arguments: Argument = emptyArray(),
    override val dependency: LatexPackage = LatexPackage.DEFAULT,
    override val display: String? = null,
    override val isMathMode: Boolean = false,
    val collapse: Boolean = false
) : LatexCommand {

    LSTINPUTLISTING("lstinputlisting", "options".asOptional(), RequiredFileArgument("filename", false, commaSeparatesArguments = false), dependency = LISTINGS),
    DIRECTLUA("directlua", "lua code".asRequired(), dependency = LUACODE),
    LUAEXEC("luaexec", "lua code".asRequired(), dependency = LUACODE),
    PY("py", dependency = PYTHONTEX),
    PYB("pyb", dependency = PYTHONTEX),
    PYC("pyc", dependency = PYTHONTEX),
    PYS("pys", dependency = PYTHONTEX),
    PYV("pyv", dependency = PYTHONTEX),
    ;

    override val identifier: String
        get() = name
}