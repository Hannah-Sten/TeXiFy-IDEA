package nl.hannahsten.texifyidea.util

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
        map[args[i]] = args[i + 1]
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
    return entries.asSequence()
        .filter { (_, v) -> v == value }
        .map { it.key }
        .toSet()
}

/**
 * Finds at least `amount` elements matching the given predicate.
 *
 * @param amount
 *          How many items the collection must contain at least in order to return true. Must be nonnegative.
 * @return `true` when `amount` or more elements in the collection match the given predicate.
 */
inline fun <T> Collection<T>.findAtLeast(amount: Int, predicate: (T) -> Boolean): Boolean {
    require(amount >= 0) { "Amount must be positive." }

    // Edge cases.
    when (amount) {
        0 -> none(predicate)
        1 -> any(predicate)
    }

    // More than 1 item, iterate.
    var matches = 0
    for (element in this) {
        if (predicate(element)) {
            matches += 1
            if (matches >= amount) {
                return true
            }
        }
    }

    return false
}

/**
 * Checks if all given predicates can be matched at least once.
 *
 * @return `true` if all predicates match for at least 1 element in the collection, `false` otherwise.
 */
inline fun <T> Collection<T>.anyMatchAll(predicate: (T) -> Boolean, vararg predicates: (T) -> Boolean): Boolean {
    val matches = BooleanArray(predicates.size + 1)
    var matchCount = 0
    for (element in this) {
        for (i in predicates.indices) {
            if (!matches[i] && predicates[i](element)) {
                matches[i] = true
                matchCount += 1
            }
        }

        if (!matches.last() && predicate(element)) {
            matches[matches.size - 1] = true
            matchCount += 1
        }
    }
    return matchCount == matches.size
}

/**
 * Checks if the map contains the given value as either a key or value.
 */
fun <T> Map<T, T>.containsKeyOrValue(value: T) = containsKey(value) || containsValue(value)

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

/**
 * Converts the collection to a vector.
 */
fun <T> Collection<T>.toVector() = Vector(this)

inline fun <reified S> Array<out Any>.filterTyped(predicate: (S) -> Boolean): List<S> {
    val result = mutableListOf<S>()
    for (item in this) {
        if (item is S && predicate(item)) {
            result.add(item)
        }
    }
    return result
}