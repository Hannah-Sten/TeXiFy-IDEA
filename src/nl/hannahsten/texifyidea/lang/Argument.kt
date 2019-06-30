package nl.hannahsten.texifyidea.lang

/**
 * @author Sten Wessel
 */
abstract class Argument @JvmOverloads protected constructor(val name: String, val type: Type = Type.NORMAL) {

    abstract override fun toString(): String

    enum class Type {
        NORMAL,
        FILE,
        TEXT
    }
}
