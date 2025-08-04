package nl.hannahsten.texifyidea.util.magic

import nl.hannahsten.texifyidea.lang.LatexPackage
import nl.hannahsten.texifyidea.lang.commands.LatexRegularCommand

object ColorMagic {

    /**
     * All commands that have a color as an argument.
     */
    val takeColorCommands = LatexRegularCommand.values()
        .filter { cmd -> cmd.arguments.map { it.name }.contains("color") }
        .map { it.commandWithSlash }.toSet()

    /**
     * All commands that define a new color.
     */
    val colorDefinitions = LatexRegularCommand.values()
        .filter { cmd -> cmd.dependency == LatexPackage.XCOLOR }
        .filter { cmd -> cmd.arguments.any { it.name == "name" } }
        .map { it.commandWithSlash }.toSet()

    val colorCommands = takeColorCommands + colorDefinitions

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