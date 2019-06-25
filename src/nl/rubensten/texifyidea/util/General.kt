package nl.rubensten.texifyidea.util

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.util.TextRange
import java.util.regex.Pattern

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
fun IntRange.toTextRange() = TextRange(this.start, this.endInclusive + 1)

/**
 * Get the length of an [IntRange].
 */
val IntRange.length: Int
    get() = endInclusive - start

/**
 * Converts a [TextRange] to [IntRange].
 */
fun TextRange.toIntRange() = startOffset..endOffset

/**
 * Easy access to [java.util.regex.Matcher.matches].
 */
fun Pattern.matches(sequence: CharSequence?) = if (sequence != null) matcher(sequence).matches() else false