package nl.hannahsten.texifyidea.util

/**
 * @author Hannah Schellekens
 */
open class Container<T> @JvmOverloads constructor(
    /**
     * The item that is contained in the container (Duh).
     */
    var item: T? = null
) {

    override fun toString() = "Container[$item]"

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Container<*>) return false
        return item == other.item
    }

    override fun hashCode() = item?.hashCode() ?: 0
}