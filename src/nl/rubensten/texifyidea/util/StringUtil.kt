package nl.rubensten.texifyidea.util

import org.intellij.lang.annotations.Language
import java.util.regex.Pattern

/**
 * Matches leading whitespace.
 */
private val LEADING_WHITESPACE: Pattern = Pattern.compile("^\\s*")

/**
 * Matches newlines.
 */
private val NEWLINE: Pattern = Pattern.compile("\\n")

/**
 * Capitalises the first character of the string.
 */
fun String.capitalizeFirst(): String = this[0].toUpperCase() + substring(1, length)

/**
 * Converts the string to camel case.
 */
fun String.camelCase(): String {
    @Language("RegExp")
    val parts = toLowerCase().split(Regex("[_\\s]+"))

    val sb = StringBuilder(parts[0])
    for (i in 1 until parts.size) {
        sb.append(parts[i].capitalizeFirst())
    }

    return sb.toString()
}

/**
 * Repeats the given string `count` amount of times.
 */
fun String.repeat(count: Int): String = TexifyUtil.fill(this, count)

/**
 * Takes the substring, but with inverted index, i.e. the index of the first character is `length`, the last index is `0`.
 */
fun String.substringEnd(startIndex: Int): String = substring(0, length - startIndex)

/**
 * Takes the substring, but with inverted index, i.e. the index of the first character is `length`, the last index is `0`.
 */
fun String.substringEnd(startIndex: Int, endIndex: Int): String = substring(length - endIndex, length - startIndex)

/**
 * Takes the substring, but with inverted index, i.e. the index of the first character is `length`, the last index is `0`.
 */
fun String.substringEnd(range: IntRange): String = substringEnd(range.start, range.endInclusive + 1)

/**
 * Returns the leading whitespace of a string.
 */
fun String.getIndent(): String {
    val matcher = LEADING_WHITESPACE.matcher(this);
    return if (matcher.find()) matcher.group(0) else ""
}

/**
 * Removes unnecessary indents.
 *
 * Meaning that when all lines have at least `x` characters, `x` characters will be removed.
 *
 * @return All lines with shared indents removed.
 */
fun List<String>.removeIndents(): List<String> {
    if (isEmpty()) {
        return this
    }

    val list = ArrayList<String>(size)
    val (maxIndent, _) = this
            .filter { !it.isBlank() }
            .map { Pair(it.getIndent().length, it) }
            .minBy { it.first } ?: return this

    for (originalLine in this) {
        if (originalLine.isBlank()) {
            continue
        }

        list.add(originalLine.substring(maxIndent))
    }

    return list
}

/**
 * Removes unnecessary indents.
 *
 * Meaning that when all lines have at least `x` characters, `x` characters will be removed.
 *
 * @return All lines with shared indents removed.
 */
fun String.removeIndents() = NEWLINE.split(this).toList().removeIndents().joinToString("\n")