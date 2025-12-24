package nl.hannahsten.texifyidea.lang.magic

/**
 * @author Hannah Schellekens
 */
open class CustomMagicKey<Key>(override val key: Key) : MagicKey<Key> {

    override fun toString() = key.toString()

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is CustomMagicKey<*>) return false
        return key == other.key
    }

    override fun hashCode(): Int = key?.hashCode() ?: 0
}