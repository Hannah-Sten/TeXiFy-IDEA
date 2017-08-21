package nl.rubensten.texifyidea.util

import java.util.*

/**
 * Puts all the elements of an array into a mutable map.
 *
 * The format is `key0, value0, key1, value1, ...`. This means that there must always be an even amount of elements.
 *
 * @return A map mapping `keyN` to `valueN` (see description above). When there are no elements, an empty map will be
 *          returned
 * @throws IllegalArgumentException When there is an odd amount of elements in the array.
 */
@Throws(IllegalArgumentException::class)
fun <T> mutableMapOfArray(args: Array<out T>): MutableMap<T, T> {
    if (args.isEmpty()) {
        return HashMap()
    }

    if (args.size % 2 != 0) {
        throw IllegalArgumentException("Must have an even number of elements, got ${args.size} instead.")
    }

    val map: MutableMap<T, T> = HashMap()
    for (i in 0 until args.size - 1 step 2) {
        map.put(args[i], args[i + 1])
    }

    return map
}

/**
 * Puts some elements into a mutable map.
 *
 * The format is `key0, value0, key1, value1, ...`. This means that there must always be an even amount of elements.
 *
 * @return A map mapping `keyN` to `valueN` (see description above). When there are no elements, an empty map will be
 *          returned
 * @throws IllegalArgumentException When there is an odd amount of elements in the array.
 */
fun <T> mutableMapOfVarargs(vararg args: T): MutableMap<T, T> = mutableMapOfArray(args)

/**
 * Puts some into a mutable map.
 *
 * The format is `key0, value0, key1, value1, ...`. This means that there must always be an even amount of elements.
 *
 * @return A map mapping `keyN` to `valueN` (see description above). When there are no elements, an empty map will be
 *          returned
 * @throws IllegalArgumentException When there is an odd amount of elements in the array.
 */
fun <T> mapOfVarargs(vararg args: T): Map<T, T> = mutableMapOfArray(args)

/**
 * Gets a random element from the list using the given random object.
 */
fun <T> List<T>.randomElement(random: Random): T = this[random.nextInt(this.size)]