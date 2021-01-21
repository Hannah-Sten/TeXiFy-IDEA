package nl.hannahsten.texifyidea.util.magic

import nl.hannahsten.texifyidea.lang.Environment
import nl.hannahsten.texifyidea.lang.commands.LatexCommand

/**
 * Saves typing.
 */
internal val LatexCommand.cmd: String
    get() = this.commandWithSlash

/**
 * Saves typing.
 */
internal val Environment.env: String
    get() = this.environmentName