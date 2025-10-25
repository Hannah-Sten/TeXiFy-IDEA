package nl.hannahsten.texifyidea.util.magic

import nl.hannahsten.texifyidea.lang.LatexContexts
import nl.hannahsten.texifyidea.lang.LatexLib
import nl.hannahsten.texifyidea.lang.introduces
import nl.hannahsten.texifyidea.lang.predefined.AllPredefined

object ColorMagic {

    /**
     * All commands that have a color as an argument.
     */
    val takeColorCommands = AllPredefined.allCommands
        .filter { cmd ->
            cmd.arguments.any {
                it.contextSignature.introduces(LatexContexts.ColorReference)
            }
        }.associateBy { it.nameWithSlash }

    /**
     * All commands that define a new color.
     */
    val colorDefinitions = AllPredefined.allCommands
        .filter { it.dependency == LatexLib.XCOLOR }
        .filter { it.introduces(LatexContexts.ColorDefinition) }
        .associateBy { it.nameWithSlash }

    val defaultXcolors = mapOf(
        "red" to 0xff0000,
        "green" to 0x00ff00,
        "blue" to 0x0000ff,
        "cyan" to 0x00ffff,
        "magenta" to 0xff00ff,
        "yellow" to 0xffff00,
        "black" to 0x000000,
        "gray" to 0x808080,
        "white" to 0xffffff,
        "darkgray" to 0x404040,
        "lightgray" to 0xbfbfbf,
        "brown" to 0xfb8040,
        "lime" to 0xbfff00,
        "olive" to 0x808000,
        "orange" to 0xff8000,
        "pink" to 0xffbfbf,
        "purple" to 0xbf0040,
        "teal" to 0x008080,
        "violet" to 0x800080
    )
}