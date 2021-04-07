package nl.hannahsten.texifyidea.lang.commands

import nl.hannahsten.texifyidea.lang.LatexPackage

/**
 * @author Hannah Schellekens
 */
enum class LatexNewDefinitionCommand(
        override val command: String,
        override vararg val arguments: Argument = emptyArray(),
        override val dependency: LatexPackage = LatexPackage.DEFAULT,
        override val display: String? = null,
        override val isMathMode: Boolean = false,
        val collapse: Boolean = false
) : LatexCommand {

    CATCODE("catcode"),
    NEWCOMMAND("newcommand", "cmd".asRequired(), "args".asOptional(), "default".asOptional(), "def".asRequired(Argument.Type.TEXT)),
    NEWCOMMAND_STAR("newcommand*", "cmd".asRequired(), "args".asOptional(), "default".asOptional(), "def".asRequired(Argument.Type.TEXT)),
    NEWIF("newif", "cmd".asRequired()),
    PROVIDECOMMAND("providecommand", "cmd".asRequired(), "args".asOptional(), "default".asOptional(), "def".asRequired(Argument.Type.TEXT)),
    PROVIDECOMMAND_STAR("providecommand*", "cmd".asRequired(), "args".asOptional(), "default".asOptional(), "def".asRequired(Argument.Type.TEXT)),
    RENEWCOMMAND("renewcommand", "cmd".asRequired(), "args".asOptional(), "default".asOptional(), "def".asRequired(Argument.Type.TEXT)),
    RENEWCOMMAND_STAR("renewcommand*", "cmd".asRequired(), "args".asOptional(), "default".asOptional(), "def".asRequired(Argument.Type.TEXT)),
    NEWENVIRONMENT("newenvironment", "name".asRequired(), "args".asOptional(), "default".asOptional(), "begdef".asRequired(Argument.Type.TEXT), "enddef".asRequired(Argument.Type.TEXT)),
    RENEWENVIRONMENT("renewenvironment", "name".asRequired(), "args".asOptional(), "default".asOptional(), "begdef".asRequired(Argument.Type.TEXT), "enddef".asRequired(Argument.Type.TEXT)),
    ;

    override val identifyer: String
        get() = name
}