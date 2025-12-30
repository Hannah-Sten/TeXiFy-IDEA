package nl.hannahsten.texifyidea.util

import java.util.*

/**
 * @author Hannah Schellekens
 */
private val ROMAN = TreeMap<Int, String>().apply {
    this[1000] = "M"
    this[500] = "D"
    this[100] = "C"
    this[50] = "L"
    this[10] = "X"
    this[5] = "V"
    this[1] = "I"
}

/**
 * Turns a given integer into a roman numeral.
 *
 * @receiver The (positive) integer to convert to roman.
 * @return The roman representation of said integer.
 * @throws IllegalArgumentException
 *          When the integer is smaller or equal to 0.
 */
fun Int.toRoman(): String {
    check(this > 0) { "Integer must be positive, got $this" }

    val fromMap = ROMAN.floorKey(this)
    return if (this == fromMap) {
        ROMAN[this]!!
    }
    else ROMAN[fromMap] + (this - fromMap!!).toRoman()
}

/**
 * @see Integer.toHexString
 */
fun Int.toHexString(): String = Integer.toHexString(this)

/**
 * Return true if [this] has a non-empty overlap with [other].
 */
fun IntRange.overlaps(other: IntRange): Boolean = this.last >= other.first && other.last >= this.first

/**
 * Merge [this] with all [others] to result in one IntRange, assuming that [this] overlaps with all [others].
 */
fun IntRange.merge(others: Iterable<IntRange>): IntRange {
    val all = mutableSetOf(this).also { it.addAll(others) }
    return IntRange(all.minByOrNull { it.first }!!.first, all.maxByOrNull { it.last }!!.last)
}