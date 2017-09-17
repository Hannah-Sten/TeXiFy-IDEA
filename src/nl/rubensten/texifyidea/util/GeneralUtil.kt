package nl.rubensten.texifyidea.util

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.util.TextRange

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
 * @see TexifyUtil.toRoman
 */
@Throws(IllegalArgumentException::class)
fun Int.toRoman(): String = TexifyUtil.toRoman(this)

/**
 * @see Integer.toHexString
 */
fun Int.toHex(): String = Integer.toHexString(this)

/**
 * Executes the given run write action.
 */
fun runWriteAction(writeAction: () -> Unit) {
    ApplicationManager.getApplication().runWriteAction(writeAction)
}

/**
 * Converts the int range to a text range.
 */
fun IntRange.toTextRange() = TextRange(start, endInclusive)