package nl.hannahsten.texifyidea.lang.commands

import nl.hannahsten.texifyidea.lang.LatexPackage
import nl.hannahsten.texifyidea.lang.LatexPackage.Companion.AMSMATH
import nl.hannahsten.texifyidea.lang.LatexPackage.Companion.STMARYRD

/**
 * @author Hannah Schellekens
 */
enum class LatexDelimiterCommand(
    override val command: String,
    val matchingName: String,
    override vararg val arguments: Argument = emptyArray(),
    override val dependency: LatexPackage = LatexPackage.DEFAULT,
    override val display: String? = null,
    override val isMathMode: Boolean = true,
    val collapse: Boolean = false
) : LatexCommand {

    LEFT_PARENTH("left(", "right)", display = "("),
    RIGHT_PARENTH("right)", "left(", display = ")"),
    LEFT_BRACKET("left[", "right]", display = "["),
    RIGHT_BRACKET("right]", "left[", display = "]"),
    LEFT_BRACE("left\\{", "right\\}", display = "{"),
    RIGHT_BRACE("right\\}", "left\\{", display = "}"),
    LEFT_ANGULAR("left<", "right>", display = "<"),
    RIGHT_ANGULAR("right>", "left<", display = ">"),
    LEFT_PIPE("left|", "right|", display = "|"),
    RIGHT_PIPE("right|", "left|", display = "|"),
    LEFT_DOUBLE_PIPE("left\\|", "right\\|", display = "||"),
    RIGHT_DOUBLE_PIPE("right\\|", "left\\|", display = "||"),
    LEFT_ANGLE_BRACKET("langle", "rangle", display = "<"),
    RIGHT_ANGLE_BRACKET("rangle", "langle", display = ">"),
    LEFT_CEIL("lceil", "rceil", display = "⌈"),
    RIGHT_CEIL("rceil", "lceil", display = "⌉"),
    LEFT_FLOOR("lfloor", "rfloor", display = "⌊"),
    RIGHT_FLOOR("rfloor", "lfloor", display = "⌋"),
    LEFT_MOUSTACHE("lmoustache", "rmoustache", display = "⎰"),
    RIGHT_MOUSTACHE("rmoustache", "lmoustache", display = "⎱"),
    LEFT_GROUP("lgroup", "rgroup"),
    RIGHT_GROUP("rgroup", "lgroup"),
    LEFT_VERTICAL_LINE("lvert", "rvert", dependency = AMSMATH),
    RIGHT_VERTICAL_LINE("rvert", "lvert", dependency = AMSMATH),
    LEFT_DOUBLE_VERTICAL_LINES("lVert", "rVert", dependency = AMSMATH),
    RIGHT_DOUBLE_VERTICAL_LINES("rVert", "lVert", dependency = AMSMATH),
    LEFT_BAG("lbag", "rbag", display = "⟅", dependency = STMARYRD),
    RIGHT_BAG("rbag", "lbag", display = "⟆", dependency = STMARYRD),
    LEFT_BAG_BOLD("Lbag", "Rbag", display = "⟅", dependency = STMARYRD),
    RIGHT_BAG_BOLD("Rbag", "Lbag", display = "⟆", dependency = STMARYRD),
    LEFT_BRACKET_BOLD("llbracket", "rrbracket", display = "⟦", dependency = STMARYRD),
    RIGHT_BRACKET_BOLD("rrbracket", "llbracket", display = "⟧", dependency = STMARYRD),
    LEFT_CEIL_BOLD("llceil", "rrceil", dependency = STMARYRD),
    RIGHT_CEIL_BOLD("rrceil", "llceil", dependency = STMARYRD),
    LEFT_FLOOR_BOLD("llfloor", "rrfloor", dependency = STMARYRD),
    RIGHT_FLOOR_BOLD("rrfloor", "llfloor", dependency = STMARYRD),
    LEFT_PARENTHESIS_BOLD("llparenthesis", "rrparenthesis", display = "⦅", dependency = STMARYRD),
    RIGHT_PARENTHESIS_BOLD("rrparenthesis", "llparenthesis", display = "⦆", dependency = STMARYRD),
    ;

    override val identifier: String
        get() = name

    val isLeft = this.command.startsWith("l")
    val isRight = this.command.startsWith("r")
}