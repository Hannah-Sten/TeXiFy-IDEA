package nl.hannahsten.texifyidea.lang

/**
 * @author Hannah Schellekens
 */
class SimpleEnvironment(
        override val environmentName: String,
        override val context: Environment.Context = Environment.Context.NORMAL,
        override val initialContents: String = "",
        override val arguments: Array<out Argument> = emptyArray(),
        override val dependency: Package = Package.DEFAULT
) : Environment {

    constructor(environmentName: String) : this(environmentName, context = Environment.Context.NORMAL)
}