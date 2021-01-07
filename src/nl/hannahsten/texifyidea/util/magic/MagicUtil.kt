package nl.hannahsten.texifyidea.util.magic

import nl.hannahsten.texifyidea.lang.Environment
import nl.hannahsten.texifyidea.lang.LatexCommand

/**
 * Saves typing.
 */
val LatexCommand.cmd: String
    get() = this.commandWithSlash


/**
 * Saves typing.
 */
val Environment.env: String
    get() = this.environmentName