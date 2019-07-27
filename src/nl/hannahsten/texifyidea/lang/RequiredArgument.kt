package nl.hannahsten.texifyidea.lang

/**
 * @author Sten Wessel
 */
@Suppress("ConvertSecondaryConstructorToPrimary")
open class RequiredArgument : Argument {

    @JvmOverloads
    internal constructor(name: String, type: Argument.Type = Argument.Type.NORMAL) : super(name, type)

    override fun toString() = "{$name}"
}