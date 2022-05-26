package nl.hannahsten.texifyidea.lang.commands

import nl.hannahsten.texifyidea.lang.LatexPackage
import nl.hannahsten.texifyidea.lang.LatexPackage.Companion.AMSMATH
import nl.hannahsten.texifyidea.lang.LatexPackage.Companion.STMARYRD

/**
 * @author Hannah Schellekens
 */
enum class LatexDelimiterCommand(
        override val command: String,
        override vararg val arguments: Argument = emptyArray(),
        override val dependency: LatexPackage = LatexPackage.DEFAULT,
        override val display: String? = null,
        override val isMathMode: Boolean = true,
        val collapse: Boolean = false
) : LatexCommand {

    LEFT_PARENTH("left(", display = "("),
    RIGHT_PARENTH("right)", display = ")"),
    LEFT_BRACKET("left[", display = "["),
    RIGHT_BRACKET("right]", display = "]"),
    LEFT_BRACE("left\\{", display = "{"),
    RIGHT_BRACE("right\\}", display = "}"),
    LEFT_ANGULAR("left<", display = "<"),
    RIGHT_ANGULAR("right>", display = ">"),
    LEFT_PIPE("left|", display = "|"),
    RIGHT_PIPE("right|", display = "|"),
    LEFT_DOUBLE_PIPE("left\\|", display = "||"),
    RIGHT_DOUBLE_PIPE("right\\|", display = "||"),
    LEFT_ANGLE_BRACKET("langle", display = "<"),
    RIGHT_ANGLE_BRACKET("rangle", display = ">"),
    LEFT_CEIL("lceil", display = "⌈"),
    RIGHT_CEIL("rceil", display = "⌉"),
    LEFT_FLOOR("lfloor", display = "⌊"),
    RIGHT_FLOOR("rfloor", display = "⌋"),
    LEFT_MOUSTACHE("lmoustache", display = "⎰"),
    RIGHT_MOUSTACHE("rmoustache", display = "⎱"),
    LEFT_GROUP("lgroup"),
    RIGHT_GROUP("rgroup"),
    LEFT_VERTICAL_LINE("lvert", dependency = AMSMATH),
    RIGHT_VERTICAL_LINE("rvert", dependency = AMSMATH),
    LEFT_DOUBLE_VERTICAL_LINES("lVert", dependency = AMSMATH),
    RIGHT_DOUBLE_VERTICAL_LINES("rVert", dependency = AMSMATH),
    LEFT_BAG("lbag", display = "⟅", dependency = STMARYRD),
    RIGHT_BAG("rbag", display = "⟆", dependency = STMARYRD),
    LEFT_BAG_BOLD("Lbag", display = "⟅", dependency = STMARYRD),
    RIGHT_BAG_BOLD("Rbag", display = "⟆", dependency = STMARYRD),
    LEFT_BRACKET_BOLD("llbracket", display = "⟦", dependency = STMARYRD),
    RIGHT_BRACKET_BOLD("rrbracket", display = "⟧", dependency = STMARYRD),
    LEFT_CEIL_BOLD("llceil", dependency = STMARYRD),
    RIGHT_CEIL_BOLD("rrceil", dependency = STMARYRD),
    LEFT_FLOOR_BOLD("llfloor", dependency = STMARYRD),
    RIGHT_FLOOR_BOLD("rrfloor", dependency = STMARYRD),
    LEFT_PARENTHESIS_BOLD("llparenthesis", display = "⦅", dependency = STMARYRD),
    RIGHT_PARENTHESIS_BOLD("rrparenthesis", display = "⦆", dependency = STMARYRD),
    ;

    override val identifier: String
        get() = name
}