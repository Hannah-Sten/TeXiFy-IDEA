package nl.hannahsten.texifyidea.util

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.util.TextRange
import java.util.regex.Pattern

/**
 * Returns `1` when `true`, returns `0` when `false`.
 */
val Boolean.int: Int
    get() = if (this) 1 else 0

/**
 * Creates a pair of two objects, analogous to [to].
 */
infix fun <T1, T2> T1.and(other: T2) = Pair(this, other)

/**
 * Prints the object in default string presentation to the console.
 */
fun Any.print() = print(this)

/**
 * Prints the object in default string presentation to the console including line feed.
 */
fun Any.println() = println(this)

/**
 * Prints `message: OBJECT` to the console.
 */
infix fun Any.debug(message: Any) = print("$message: $this")

/**
 * Prints `message: OBJECT` to the console including line feed.
 */
infix fun Any.debugln(message: Any) = println("$message: $this")

/**
 * Executes the given run write action.
 */
fun runWriteAction(writeAction: () -> Unit) {
    ApplicationManager.getApplication().runWriteAction(writeAction)
}

/**
 * Converts an [IntRange] to [TextRange].
 */
fun IntRange.toTextRange() = TextRange(this.first, this.last + 1)

/**
 * Get the length of an [IntRange].
 */
val IntRange.length: Int
    get() = endInclusive - start

/**
 * Converts the range to a range representation with the given seperator.
 * When the range has size 0, it will only print the single number.
 */
fun IntRange.toRangeString(separator: String = "-") = if (start == endInclusive) start else "$start$separator$endInclusive"

/**
 * Shift the range to the right by the number of places given.
 */
fun IntRange.shiftRight(displacement: Int): IntRange {
    return (this.first + displacement)..(this.last + displacement)
}

/**
 * Converts a [TextRange] to [IntRange].
 */
fun TextRange.toIntRange() = startOffset..endOffset

/**
 * Easy access to [java.util.regex.Matcher.matches].
 */
fun Pattern.matches(sequence: CharSequence?) = if (sequence != null) matcher(sequence).matches() else false