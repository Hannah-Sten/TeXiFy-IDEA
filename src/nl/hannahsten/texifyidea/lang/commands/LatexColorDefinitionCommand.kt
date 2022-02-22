package nl.hannahsten.texifyidea.lang.commands

import nl.hannahsten.texifyidea.lang.LatexPackage
import nl.hannahsten.texifyidea.lang.LatexPackage.Companion.COLOR
import nl.hannahsten.texifyidea.lang.LatexPackage.Companion.XCOLOR

/**
 * @author Hannah Schellekens
 */
enum class LatexColorDefinitionCommand(
        override val command: String,
        override vararg val arguments: Argument = emptyArray(),
        override val dependency: LatexPackage = LatexPackage.DEFAULT,
        override val display: String? = null,
        override val isMathMode: Boolean = false,
        val collapse: Boolean = false
) : LatexCommand {

    DEFINECOLOR("definecolor", "type".asOptional(), "name".asRequired(), "model-list".asRequired(), "spec-list".asRequired(), dependency = COLOR),
    PROVIDECOLOR("providecolor", "type".asOptional(), "name".asRequired(), "model-list".asRequired(), "spec-list".asRequired(), dependency = XCOLOR),
    COLORLET("colorlet", "type".asOptional(), "name".asRequired(), "num model".asOptional(), "color".asRequired(), dependency = XCOLOR),
    DEFINECOLORSET("definecolorset", "type".asOptional(), "model-list".asRequired(), "head".asRequired(), "tail".asRequired(), "set spec".asRequired(), dependency = XCOLOR),
    PROVIDECOLORSET("providecolorset", "type".asOptional(), "model-list".asRequired(), "head".asRequired(), "tail".asRequired(), "set spec".asRequired(), dependency = XCOLOR),
    PREPARECOLOR("preparecolor", "type".asOptional(), "name".asRequired(), "model-list".asRequired(), "spec-list".asRequired(), dependency = XCOLOR),
    PREPARECOLORSET("preparecolorset", "type".asOptional(), "model-list".asRequired(), "head".asRequired(), "tail".asRequired(), "set spec".asRequired(), dependency = XCOLOR),
    DEFINE_NAMED_COLOR("DefineNamedColor", "type".asRequired(), "name".asRequired(), "model-list".asRequired(), "spec-list".asRequired(), dependency = COLOR),
    DEFINECOLORS("definecolors", "id-list".asRequired(), dependency = XCOLOR),
    PROVIDECOLORS("providecolors", "id-list".asRequired(), dependency = XCOLOR),
    DEFINECOLORSERIES("definecolorseries", "name".asRequired(), "core model".asRequired(), "method".asRequired(), "b-model".asOptional(), "b-spec".asRequired(), "s-model".asRequired(), "s-spec".asRequired(), dependency = XCOLOR),
    ;

    override val identifier: String
        get() = name
}