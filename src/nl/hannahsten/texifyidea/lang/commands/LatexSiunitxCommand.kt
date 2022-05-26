package nl.hannahsten.texifyidea.lang.commands

import nl.hannahsten.texifyidea.lang.LatexPackage
import nl.hannahsten.texifyidea.lang.LatexPackage.Companion.SIUNITX

/**
 * @author Hannah Schellekens
 */
enum class LatexSiunitxCommand(
        override val command: String,
        override vararg val arguments: Argument = emptyArray(),
        override val dependency: LatexPackage = LatexPackage.DEFAULT,
        override val display: String? = null,
        override val isMathMode: Boolean = false,
        val collapse: Boolean = false
) : LatexCommand {

    ANG("ang", "options".asOptional(), "angle".asRequired(), dependency = SIUNITX),
    NUM("num", "options".asOptional(), "number".asRequired(), dependency = SIUNITX),
    SI("si", "options".asOptional(), "unit".asRequired(), dependency = SIUNITX),
    SI_NUM("SI", "options".asOptional(), "number".asRequired(), "pre-unit".asOptional(), "unit".asRequired(), dependency = SIUNITX),
    NUMLIST("numlist", "options".asOptional(), "numbers".asRequired(), dependency = SIUNITX),
    NUMRANGE("numrange", "options".asOptional(), "number1".asRequired(), "number2".asRequired(), dependency = SIUNITX),
    SILIST("SIlist", "options".asOptional(), "numbers".asRequired(), "unit".asRequired(), dependency = SIUNITX),
    SIRANGE("numrange", "options".asOptional(), "number1".asRequired(), "number2".asRequired(), "unit".asRequired(), dependency = SIUNITX),
    SISETUP("sisetup", "options".asRequired(), dependency = SIUNITX),
    TABLENUM("tablenum", "options".asOptional(), "number".asRequired(), dependency = SIUNITX),
    ;

    override val identifier: String
        get() = name
}