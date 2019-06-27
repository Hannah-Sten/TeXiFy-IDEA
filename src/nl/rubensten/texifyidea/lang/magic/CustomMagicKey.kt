package nl.rubensten.texifyidea.lang.magic

/**
 * @author Ruben Schellekens
 */
open class CustomMagicKey<Key>(override val key: Key) : MagicKey<Key> {

    override fun toString() = key.toString()

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is CustomMagicKey<*>) return false
        if (key != other.key) return false
        return true
    }

    override fun hashCode(): Int {
        return key?.hashCode() ?: 0
    }
}