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
    UNIT("unit", "options".asOptional(), "unit".asRequired(), dependency = SIUNITX),
    QTY("qty", "options".asOptional(), "number".asRequired(), "unit".asRequired(), dependency = SIUNITX),
    MICRO("micro", display = "µ", dependency = SIUNITX),
    NUMLIST("numlist", "options".asOptional(), "numbers".asRequired(), dependency = SIUNITX),
    NUMPRODUCT("numproduct", "options".asOptional(), "numbers".asRequired(), dependency = SIUNITX),
    NUMRANGE("numrange", "options".asOptional(), "number1".asRequired(), "number2".asRequired(), dependency = SIUNITX),
    QTYLIST("qtylist", "options".asOptional(), "numbers".asRequired(), "unit".asRequired(), dependency = SIUNITX),
    QTYPRODUCT("qtyproduct", "options".asOptional(), "numbers".asRequired(), "unit".asRequired(), dependency = SIUNITX),
    QTYRANGE("qtyrange", "options".asOptional(), "number1".asRequired(), "number2".asRequired(), "unit".asRequired(), dependency = SIUNITX),
    COMPLEXNUM("complexnum", "options".asOptional(), "number".asRequired(), dependency = SIUNITX),
    COMPLEXQTY("complexqty", "options".asOptional(), "number".asRequired(), "unit".asRequired(), dependency = SIUNITX),
    SISETUP("sisetup", "options".asRequired(), dependency = SIUNITX),
    TABLENUM("tablenum", "options".asOptional(), "number".asRequired(), dependency = SIUNITX),
    ;

    override val identifier: String
        get() = name
}
