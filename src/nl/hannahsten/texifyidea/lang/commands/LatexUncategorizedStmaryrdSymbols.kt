package nl.hannahsten.texifyidea.lang.commands

import nl.hannahsten.texifyidea.lang.LatexPackage
import nl.hannahsten.texifyidea.lang.LatexPackage.Companion.STMARYRD

/**
 * @author Hannah Schellekens
 */
enum class LatexUncategorizedStmaryrdSymbols(
    override val command: String,
    override vararg val arguments: Argument = emptyArray(),
    override val dependency: LatexPackage = LatexPackage.DEFAULT,
    override val display: String? = null,
    override val isMathMode: Boolean = true,
    val collapse: Boolean = false
) : LatexCommand {

    Y_LEFT("Yleft", dependency = STMARYRD),
    Y_UP("Yup", dependency = STMARYRD),
    Y_RIGHT("Yright", dependency = STMARYRD),
    Y_DOWN("Ydown", dependency = STMARYRD),
    BIN_AMPERSAND("binampersand", dependency = STMARYRD, display = "&", collapse = true),
    BIN_REVERSED_AMPERSAND("bindnasrepma", dependency = STMARYRD),
    O_BAR("baro", dependency = STMARYRD, display = "⏀", collapse = true),
    BOX_BAR("boxbar", dependency = STMARYRD),
    BOX_CIRCLE("boxcircle", dependency = STMARYRD),
    BOX_BOX("boxbox", dependency = STMARYRD),
    BOXED_DOT_STMARYRD("boxdot", dependency = STMARYRD),
    BOX_ASTERISK("boxast", dependency = STMARYRD),
    BOX_BSLASH("boxbslash", dependency = STMARYRD),
    BOX_EMPTY("boxempty", dependency = STMARYRD, display = "◻", collapse = true),
    BOX_SLASH("boxslash", dependency = STMARYRD),
    BIG_BOX("bigbox", dependency = STMARYRD),
    CURLY_VEE_VARIANT("varcurlyvee", dependency = STMARYRD),
    BIG_CURLY_VEE("bigcurlyvee", dependency = STMARYRD),
    CURLY_WEDGE_VARIANT("varcurlywedge", dependency = STMARYRD),
    BIG_CURLY_WEDGE_VARIANT("bigcurlywedge", dependency = STMARYRD),
    CURLY_VEE_DOWN_ARROW("curlyveedownarrow", dependency = STMARYRD),
    CURLY_VEE_UP_ARROW("curlyveeuparrow", dependency = STMARYRD),
    CURLY_WEDGE_DOWN_ARROW("curlywedgedownarrow", dependency = STMARYRD),
    CURLY_WEDGE_UP_ARROW("curlywedgeuparrow", dependency = STMARYRD),
    BLACKBOARD_BACKSLASH("bbslash", dependency = STMARYRD, display = "⑊", collapse = true),
    BLACKBOARD_SLASH("sslash", dependency = STMARYRD, display = "⃫", collapse = true),
    FAT_BACKSLASH("fatbslash", dependency = STMARYRD),
    FAT_SLASH("fatslash", dependency = STMARYRD),
    FAT_SEMICOLON("fatsemi", dependency = STMARYRD),
    BIG_PARALLEL("bigparallel", dependency = STMARYRD, display = "∥", collapse = true),
    INTERLEAVE("interleave", dependency = STMARYRD, display = "⫴", collapse = true),
    BIG_INTERLEAVE("biginterleave", dependency = STMARYRD, display = "⫴", collapse = true),
    TALL_OBLONG("talloblong", dependency = STMARYRD, display = "▯", collapse = true),
    LEFT_SLICE("leftslice", dependency = STMARYRD),
    RIGHT_SLICE("rightslice", dependency = STMARYRD),
    MERGE("merge", dependency = STMARYRD),
    MOO("moo", dependency = STMARYRD),
    CAP_PLUS("nplus", dependency = STMARYRD),
    BIG_CAP_PLUS("bignplus", dependency = STMARYRD),
    O_HORIZONTAL_DASH("minuso", dependency = STMARYRD, display = "⦵", collapse = true),
    O_WITH_VERTICAL_LINE("obar", dependency = STMARYRD, display = "⊖", collapse = true),
    OBLONG("oblong", dependency = STMARYRD, display = "▯", collapse = true),
    O_WITH_BACKSLASH("obslash", dependency = STMARYRD, display = "⃠", collapse = true),
    O_WITH_GREATER_THAN("ogreaterthan", dependency = STMARYRD, display = "⧁", collapse = true),
    O_WITH_LESS_THAN("olessthan", dependency = STMARYRD, display = "⧀", collapse = true),
    O_WITH_VEE("ovee", dependency = STMARYRD),
    O_WITH_WEDGE("owedge", dependency = STMARYRD),
    BIG_CIRCLE_VARIANT("varbigcirc", dependency = STMARYRD, display = "◯", collapse = true),
    O_WITH_STAR_VARIANT("varoast", dependency = STMARYRD, display = "⊛", collapse = true),
    O_WITH_VERTICAL_BAR_VARIANT("varobar", dependency = STMARYRD, display = "⦶", collapse = true),
    O_WITH_BACKSLASH_VARIANT("varobslash", dependency = STMARYRD, display = "⦸", collapse = true),
    O_WITH_CIRCLE_VARIANT("varocircle", dependency = STMARYRD, display = "⦾", collapse = true),
    O_WITH_DOT_VARIANT("varodot", dependency = STMARYRD, display = "⨀", collapse = true),
    O_WITH_GREATER_THAN_VARIANT("varogreaterthan", dependency = STMARYRD, display = "⧁", collapse = true),
    O_WITH_LESS_THAN_VARIANT("varolessthan", dependency = STMARYRD, display = "⧀", collapse = true),
    O_WITH_MINUS_VARIANT("varominus", dependency = STMARYRD, display = "⊖", collapse = true),
    O_WITH_PLUS_VARIANT("varoplus", dependency = STMARYRD, display = "⊕", collapse = true),
    O_WITH_SLASH_VARIANT("varoslash", dependency = STMARYRD, display = "⊘", collapse = true),
    O_WITH_TIMES_VARIANT("varotimes", dependency = STMARYRD, display = "⊗", collapse = true),
    O_WITH_VEE_VARIANT("varovee", dependency = STMARYRD),
    O_WITH_WEDGE_VARIANT("varowedge", dependency = STMARYRD),
    TIMES_VARIANT("vartimes", dependency = STMARYRD, display = "⨉", collapse = true),
    BIG_SQUARE_CAP("bigsqcap", dependency = STMARYRD, display = "⊓", collapse = true),
    ;

    override val identifier: String
        get() = name
}