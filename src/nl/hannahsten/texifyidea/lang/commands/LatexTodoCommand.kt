package nl.hannahsten.texifyidea.lang.commands

import nl.hannahsten.texifyidea.lang.LatexPackage
import nl.hannahsten.texifyidea.lang.LatexPackage.Companion.TODONOTES

enum class LatexTodoCommand(
    override val command: String,
    override vararg val arguments: Argument = emptyArray(),
    override val dependency: LatexPackage = LatexPackage.DEFAULT,
    override val display: String? = null,
    override val isMathMode: Boolean = false,
    val collapse: Boolean = false
) : LatexCommand {

    TODO("todo", "note".asRequired(), dependency = TODONOTES),
    MISSINGFIGURE("missingfigure", "note".asRequired(), dependency = TODONOTES),
    LISTOFTODOS("listoftodos", "name".asOptional(), dependency = TODONOTES)
    ;

    override val identifier: String
        get() = name
}