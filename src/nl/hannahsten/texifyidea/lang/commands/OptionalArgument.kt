package nl.hannahsten.texifyidea.lang.commands

/**
 * @author Sten Wessel
 */
@Suppress("ConvertSecondaryConstructorToPrimary")
class OptionalArgument : Argument {

    @JvmOverloads
    internal constructor(name: String, type: Type = Type.NORMAL) : super(name, type)

    override fun toString() = "[$name]"

    override fun equals(other: Any?): Boolean {
        return (other as? OptionalArgument)?.name == this.name
    }

    override fun hashCode(): Int {
        return this.name.hashCode()
    }
}