package nl.hannahsten.texifyidea.lang

/**
 * @author Sten Wessel
 */
@Suppress("ConvertSecondaryConstructorToPrimary")
class OptionalArgument : Argument {

    @JvmOverloads
    internal constructor(name: String, type: Argument.Type = Argument.Type.NORMAL) : super(name, type)

    override fun toString() = "[$name]"
}