package nl.hannahsten.texifyidea.lang.predefined

import nl.hannahsten.texifyidea.lang.PredefinedCommandSet

object PredefinedPairedDelimiters : PredefinedCommandSet(){

    data class Delimiter(
        val left: String,
        val right: String,
        val leftDisplay: String? = null,
        val rightDisplay: String? = null,
        val dependency : String = ""
    )
    private fun d(left: String, right: String, leftDisplay: String? = null, rightDisplay: String? = null, dependency: String = ""
    ) : Delimiter {
        return Delimiter(left, right, leftDisplay, rightDisplay)
    }

    /**
     * The predefined paired delimiters in LaTeX.
     */
    val delimiters = listOf(
        d("left(", "right)","(", ")"),
        d("left[", "right]", "[", "]"),
        d("left\\{", "right\\}", "{", "}"),
        d("left<", "right>", "<", ">"),
        d("left|", "right|", "|", "|"),
        d("left\\|", "right\\|", "||", "||"),
        d("langle", "rangle", "<", ">"),
        d("lceil", "rceil", "⌈", "⌉"),
        d("lfloor", "rfloor", "⌊", "⌋"),
        d("lmoustache", "rmoustache", "⎰", "⎱"),
        d("lgroup", "rgroup"),
        d("lvert", "rvert", dependency = "amsmath"),
        d("lVert", "rVert", dependency = "amsmath"),
        d("lbag", "rbag", "⟅", "⟆", dependency = "stmaryrd"),
        d("Lbag", "Rbag", "⟅", "⟆", dependency = "stmaryrd"),
        d("llbracket", "rrbracket", "⟦", "⟧", dependency = "stmaryrd"),
        d("llceil", "rrceil", dependency = "stmaryrd"),
        d("llfloor", "rrfloor", dependency = "stmaryrd"),
        d("llparenthesis", "rrparenthesis", "⦅", "⦆", dependency = "stmaryrd"),
    )

    val delimiterLeftMap: Map<String, Delimiter> by lazy {
        delimiters.associateBy { it.left }
    }
    val delimiterRightMap: Map<String, Delimiter> by lazy {
        delimiters.associateBy { it.right }
    }

    val delimiterCommands = mathCommands {
        delimiters.map { delimiter ->
            underPackage(delimiter.dependency){
                symbol(delimiter.left,delimiter.leftDisplay)
                symbol(delimiter.right,delimiter.rightDisplay)
            }
        }
    }

}