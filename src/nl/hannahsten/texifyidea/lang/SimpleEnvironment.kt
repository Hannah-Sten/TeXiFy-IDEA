package nl.hannahsten.texifyidea.lang

import nl.hannahsten.texifyidea.lang.commands.Argument

/**
 * @author Hannah Schellekens
 */
class SimpleEnvironment(
    override val environmentName: String,
    override val context: Environment.Context = Environment.Context.NORMAL,
    override val initialContents: String = "",
    override val arguments: Array<out Argument> = emptyArray(),
    override val dependency: LatexPackage = LatexPackage.DEFAULT,
    override val description: String = ""
) : Environment {

    constructor(environmentName: String) : this(environmentName, context = Environment.Context.NORMAL)
}