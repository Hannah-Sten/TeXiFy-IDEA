package nl.rubensten.texifyidea.util

import com.intellij.openapi.util.TextRange

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
 * Converts an [IntRange] to [TextRange].
 */
fun IntRange.toTextRange() = TextRange(this.start, this.endInclusive + 1)

/**
 * Converts a [TextRange] to [IntRange].
 */
fun TextRange.toIntRange() = startOffset..endOffset