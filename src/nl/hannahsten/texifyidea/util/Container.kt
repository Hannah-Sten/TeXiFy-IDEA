package nl.hannahsten.texifyidea.util

/**
 * @author Hannah Schellekens
 */
open class Container<T> {

    /**
     * The item that is contained in the container (Duh).
     */
    var item: T?

    @JvmOverloads
    constructor(item: T? = null) {
        this.item = item
    }

    override fun toString() = "Container[$item]"

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Container<*>) return false
        if (item != other.item) return false
        return true
    }

    override fun hashCode() = item?.hashCode() ?: 0
}