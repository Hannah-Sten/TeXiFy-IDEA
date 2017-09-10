package nl.rubensten.texifyidea.lang

/**
 * @author Ruben Schellekens
 */
class SimpleEnvironment(
        override val environmentName: String,
        override val context: Environment.Context = Environment.Context.NORMAL,
        override val initialContents: String = "",
        override val arguments: Array<out Argument> = emptyArray(),
        val `package`: Package = Package.DEFAULT
) : Environment {

    constructor(environmentName: String) : this(environmentName, context = Environment.Context.NORMAL)

    override fun getDependency() = `package`
}