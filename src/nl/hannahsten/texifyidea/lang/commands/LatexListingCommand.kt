package nl.hannahsten.texifyidea.lang.commands

import nl.hannahsten.texifyidea.lang.LatexPackage
import nl.hannahsten.texifyidea.lang.LatexPackage.Companion.LISTINGS

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

    LSTINPUTLISTING("lstinputlisting", "options".asOptional(), RequiredFileArgument("filename", false, commaSeparatesArguments = false), dependency = LISTINGS)
    ;

    override val identifyer: String
        get() = name
}