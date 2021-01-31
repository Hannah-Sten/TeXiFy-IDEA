package nl.hannahsten.texifyidea.lang.commands

import nl.hannahsten.texifyidea.lang.LatexPackage
import nl.hannahsten.texifyidea.lang.LatexPackage.Companion.MATHTOOLS

/**
 * @author Hannah Schellekens
 */
enum class LatexMathtoolsRegularCommand(
        override val command: String,
        override vararg val arguments: Argument = emptyArray(),
        override val dependency: LatexPackage = LatexPackage.DEFAULT,
        override val display: String? = null,
        override val isMathMode: Boolean = false,
        val collapse: Boolean = false
) : LatexCommand {

    DECLARE_PAIRED_DELIMITER("DeclarePairedDelimiter", "cmd".asRequired(), "left delimiter".asRequired(), "right delimiter".asRequired(), dependency = MATHTOOLS),
    DECLARE_PAIRED_DELIMITER_X("DeclarePairedDelimiterX", "cmd".asRequired(), "num args".asOptional(), "left delimiter".asRequired(), "right delimiter".asRequired(), "body".asRequired(Argument.Type.TEXT), dependency = MATHTOOLS),
    DECLARE_PAIRED_DELIMITER_XPP("DeclarePairedDelimiterXPP", "cmd".asRequired(), "num args".asOptional(), "pre code".asRequired(), "left delimiter".asRequired(), "right delimiter".asRequired(), "post code".asRequired(), "body".asRequired(Argument.Type.TEXT), dependency = MATHTOOLS),
    ;

    override val identifyer: String
        get() = name
}