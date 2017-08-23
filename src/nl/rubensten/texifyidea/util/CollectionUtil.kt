package nl.rubensten.texifyidea.util

import java.util.*
import java.util.stream.Collectors
import java.util.stream.Stream

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

/**
 * Looks up keys in the map that has the given `value`.
 *
 * @return All keys with the given value.
 */
fun <K, V> Map<K, V>.findKeys(value: V): Set<K> {
    return entries.stream()
            .filter { (_, v) -> v == value }
            .map { it.key }
            .set()
}

/**
 * Collects stream to [List].
 */
fun <T> Stream<T>.list(): List<T> = this.mutableList()

/**
 * Collects stream to [MutableList].
 */
fun <T> Stream<T>.mutableList(): MutableList<T> = this.collect(Collectors.toList())

/**
 * Collects stream to [Set].
 */
fun <T> Stream<T>.set(): Set<T> = this.mutableSet()

/**
 * Collects stream to [MutableSet]
 */
fun <T> Stream<T>.mutableSet(): MutableSet<T> = this.collect(Collectors.toSet())