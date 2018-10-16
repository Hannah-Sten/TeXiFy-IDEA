package nl.rubensten.texifyidea.util

import org.intellij.lang.annotations.Language

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
 * Repeats the given string a given amount of times.
 *
 * @param count
 *         The amount of times to repeat the string.
 */
fun String.repeat(count: Int) = buildString(count * this.length) {
    for (i in 0 until count) {
        append(this)
    }
}

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
 * Trims `startTrim` characters from the front, and `endTrim` characters from the end.
 */
fun String.trimRange(startTrim: Int, endTrim: Int): String = substring(startTrim).substringEnd(endTrim)

/**
 * Returns the leading whitespace of a string.
 */
fun String.getIndent(): String {
    val matcher = Magic.Pattern.leadingWhitespace.matcher(this);
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
    val (maxIndent, _) = asSequence()
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
fun String.removeIndents() = Magic.Pattern.newline.split(this)
        .toList()
        .removeIndents()
        .joinToString("\n")