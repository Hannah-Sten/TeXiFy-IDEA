package nl.hannahsten.texifyidea.util

import com.intellij.openapi.util.TextRange
import org.intellij.lang.annotations.Language
import java.io.IOException
import java.util.concurrent.TimeUnit
import kotlin.math.max
import kotlin.math.min

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
fun String.substringEnd(range: IntRange): String = substringEnd(range.first, range.last + 1)

/**
 * Trims `startTrim` characters from the front, and `endTrim` characters from the end.
 */
fun String.trimRange(startTrim: Int, endTrim: Int): String {
    if (startTrim + endTrim > length) return ""
    return substring(startTrim).substringEnd(endTrim)
}

/**
 * Returns the leading whitespace of a string.
 */
fun String.getIndent(): String {
    val matcher = Magic.Pattern.leadingWhitespace.matcher(this)
    return if (matcher.find()) matcher.group(0) else ""
}

/**
 * Appends an extension to a path only if the given path does not end in that extension.
 *
 * @param extensionWithoutDot
 *         The extension to append optionally.
 * @return A path ending with the given extension without duplications (e.g. `.tex.tex` is impossible.)
 */
fun String.appendExtension(extensionWithoutDot: String): String {
    if (extensionWithoutDot == "") return this

    val dottedExtension = ".${extensionWithoutDot.toLowerCase()}"
    val thisLower = toLowerCase()

    return when {
        thisLower.endsWith(dottedExtension) -> this
        endsWith('.') -> this + extensionWithoutDot
        else -> this + dottedExtension
    }
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
            .minByOrNull { it.first } ?: return this

    var noContentYet = true
    for (originalLine in this) {
        if (noContentYet && originalLine.isBlank()) {
            continue
        }

        if (originalLine.isBlank()) {
            list.add("")
        }
        else {
            noContentYet = false
            list.add(originalLine.substring(maxIndent))
        }
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

/**
 * Remove all appearances of all given strings.
 */
fun String.removeAll(vararg strings: String): String {
    var formatted = this
    strings.forEach { formatted = formatted.replace(it, "") }
    return formatted
}

/**
 * Remove [string] from [this].
 */
fun String.remove(string: String): String = this.replace(string, "")

/**
 * Formats the string as a valid filename, removing not-allowed characters, in TeX-style with - as separator.
 */
fun String.formatAsFileName(): String = this.formatAsFilePath().removeAll("/", "\\")

/**
 * Formats the string as a valid filepath, removing not-allowed characters, in TeX-style with - as separator. Any / or \ characters are not removed.
 */
fun String.formatAsFilePath(): String {
    val formatted = this.replace(" ", "-")
            .removeAll("<", ">", "\"", "|", "?", "*", ":") // Mostly just a problem on Windows
            .toLowerCase()

    // If there are no valid characters left, use a default name.
    return if (formatted.isEmpty()) "myfile" else formatted
}

/**
 * Formats the string as a valid LaTeX label name.
 */
fun String.formatAsLabel(): String {
    return replace(" ", "-")
            .removeAll("%", "~", "#", "\\")
            .toLowerCase()
}

/**
 * Split the given string on whitespace.
 */
fun String.splitWhitespace() = split(Regex("\\s+"))

/**
 * Removes HTML tags from the string.
 *
 * @return The string with HTML tags removed.
 *
 * @see [Magic.Pattern.htmlTag]
 */
fun String.removeHtmlTags() = this.replace(Magic.Pattern.htmlTag.toRegex(), "")

/**
 * Run a command in the terminal.
 *
 * @return The output of the command or null if an exception was thrown.
 */
fun String.runCommand(): String? {
    return try {
        val parts = this.split("\\s".toRegex())
        val proc = ProcessBuilder(*parts.toTypedArray())
                .redirectOutput(ProcessBuilder.Redirect.PIPE)
                .redirectError(ProcessBuilder.Redirect.PIPE)
                .start()

        // Timeout value
        proc.waitFor(10, TimeUnit.SECONDS)
        proc.inputStream.bufferedReader().readText().trim() + proc.errorStream.bufferedReader().readText().trim()
    }
    catch (e: IOException) {
        null
    }
}

/** If this contains any of the given set. */
fun CharSequence.containsAny(set: Set<String>) = set.any { this.contains(it) }

/** Shrink textrange with the given amount at both sides. */
fun TextRange.shrink(amount: Int) = TextRange(min(this.startOffset + amount, endOffset - 1), max(0, this.endOffset - amount))